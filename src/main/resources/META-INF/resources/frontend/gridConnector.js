window.Vaadin.Flow.gridConnector = {
  initLazy: function(grid) {
    // Check whether the connector was already initialized for the grid
    if (grid.$connector){
      return;
    }

    Vaadin.Grid.ItemCache.prototype.ensureSubCacheForScaledIndex = function(scaledIndex) {
      if (!this.itemCaches[scaledIndex]) {
        const subCache = new Vaadin.Grid.ItemCache(this.grid, this, this.items[scaledIndex]);
        subCache.itemkeyCaches = {};
        if(!this.itemkeyCaches) {
          this.itemkeyCaches = {};
        }
        this.itemCaches[scaledIndex] = subCache;
        this.itemkeyCaches[grid.getItemId(subCache.parentItem)] = subCache;
        this.grid._loadPage(0, subCache);
      }
    }

    Vaadin.Grid.ItemCache.prototype.getCacheAndIndexByKey = function(key) {
      for (let index in this.items) {
        if(grid.getItemId(this.items[index]) === key) {
          return {cache: this, scaledIndex: index};
        }
      }
      const keys = Object.keys(this.itemkeyCaches);
      for (let i = 0; i < keys.length; i++) {
        const expandedKey = keys[i];
        const subCache = this.itemkeyCaches[expandedKey];
        let cacheAndIndex = subCache.getCacheAndIndexByKey(key);
        if(cacheAndIndex) {
          return cacheAndIndex;
        }
      }
      return undefined;
    }

    const rootPageCallbacks = {};
    const treePageCallbacks = {};
    const cache = {};

    let lastRequestedRanges = {};
    const root = 'null';
    lastRequestedRanges[root] = [0, 0];

    const validSelectionModes = ['SINGLE', 'NONE', 'MULTI'];
    let selectedKeys = {};
    let selectionMode = 'SINGLE';

    let detailsVisibleOnClick = true;

    grid.size = 0; // To avoid NaN here and there before we get proper data

    grid.$connector = {};

    grid.$connector.doSelection = function(item, userOriginated) {
      if (selectionMode === 'NONE') {
        return;
      }
      if (userOriginated && (grid.getAttribute('disabled') || grid.getAttribute('disabled') === '')) {
          return;
      }
      if (selectionMode === 'SINGLE') {
        grid.selectedItems = [];
        selectedKeys = {};
      }
      grid.selectItem(item);
      selectedKeys[item.key] = item;
      if (userOriginated) {
          item.selected = true;
          grid.$server.select(item.key);
      } else {
          grid.fire('select', {item: item, userOriginated: userOriginated});
      }

      if (selectionMode === 'MULTI' && arguments.length > 2) {
          for (i = 2; i < arguments.length; i++) {
              grid.$connector.doSelection(arguments[i], userOriginated);
          }
      }
    };

    grid.$connector.doDeselection = function(item, userOriginated) {
      if (selectionMode === 'SINGLE' || selectionMode === 'MULTI') {
        grid.deselectItem(item);
        delete selectedKeys[item.key];
        if (userOriginated) {
          delete item.selected;
          grid.$server.deselect(item.key);
        } else {
          grid.fire('deselect', {item: item, userOriginated: userOriginated});
        }
      }

      if (selectionMode === 'MULTI' && arguments.length > 2) {
          for (i = 2; i < arguments.length; i++) {
              grid.$connector.doDeselection(arguments[i], userOriginated);
          }
      }
    };

    grid.__activeItemChanged = function(newVal, oldVal) {
      if (selectionMode != 'SINGLE') {
        return;
      }
      if (!newVal) {
        if (oldVal && selectedKeys[oldVal.key]) {
          grid.$connector.doDeselection(oldVal, true);
        }
        return;
      }
      if (!selectedKeys[newVal.key]) {
        grid.$connector.doSelection(newVal, true);
      } else {
        grid.$connector.doDeselection(newVal, true);
      }
    };
    grid._createPropertyObserver('activeItem', '__activeItemChanged', true);

    grid.__activeItemChangedDetails = function(newVal, oldVal) {
      if(!detailsVisibleOnClick) {
        return;
      }
      if (newVal && !newVal.detailsOpened) {
        grid.$server.setDetailsVisible(newVal.key);
      } else {
        grid.$server.setDetailsVisible(null);
      }
    }
    grid._createPropertyObserver('activeItem', '__activeItemChangedDetails', true);

    grid.$connector.setDetailsVisibleOnClick = function(visibleOnClick) {
      detailsVisibleOnClick = visibleOnClick;
    };

    grid._getPageIfSameLevel = function(parentKey, index, defaultPage) {
      let cacheAndIndex = grid._cache.getCacheAndIndex(index);
      let parentItem = cacheAndIndex.cache.parentItem;
      let parentKeyOfIndex = (parentItem) ? grid.getItemId(parentItem) : root;
      if(parentKey !== parentKeyOfIndex) {
        return defaultPage;
      } else {
        return grid._getPageForIndex(index);
      }
    }

    grid.getItemCacheByKey = function(key) {
      let cacheAndIndex = grid._cache.getCacheAndIndexByKey(key);
      if(cacheAndIndex) {
        return cacheAndIndex.cache;
      }
      return undefined;
    }

    grid.getItemCache = function(index) {
      let cacheAndIndex = grid._cache.getCacheAndIndex(index);
      if(cacheAndIndex && cacheAndIndex.cache) {
        let levelCache = cacheAndIndex.cache;
        let scaledIndex = cacheAndIndex.scaledIndex;
        return levelCache.itemCaches[scaledIndex];
      }
      return undefined;
    }

    grid.fetchPage = function(fetch, page, parentKey, parentIndex) {
      // Determine what to fetch based on scroll position and not only
      // what grid asked for

      // The buffer size could be multiplied by some constant defined by the user,
      // if he needs to reduce the number of items sent to the Grid to improve performance
      // or to increase it to make Grid smoother when scrolling
      let start = Math.max(0, grid._virtualStart);
      let end = Math.max(0, grid._virtualEnd);
      let buffer = end - start;

      let firstNeededIndex = Math.max(0, start + grid._vidxOffset - buffer);
      let lastNeededIndex = Math.min(end + grid._vidxOffset + buffer, grid._virtualCount);

      let firstNeededPage = page;
      let lastNeededPage = page;
      for(let idx = firstNeededIndex; idx <= lastNeededIndex; idx++) {
        firstNeededPage = Math.min(firstNeededPage, grid._getPageIfSameLevel(parentKey, idx, firstNeededPage));
        lastNeededPage = Math.max(lastNeededPage, grid._getPageIfSameLevel(parentKey, idx, lastNeededPage));
      }

      let firstPage = Math.max(0,  firstNeededPage);
      let lastPage = (parentKey !== root) ? lastNeededPage: Math.min(lastNeededPage, Math.floor(grid.size / grid.pageSize));
      let lastRequestedRange = lastRequestedRanges[parentKey];
      if(!lastRequestedRange) {
        lastRequestedRange = [-1, -1];
      }
      if (lastRequestedRange[0] != firstPage || lastRequestedRange[1] != lastPage) {
        lastRequestedRange = [firstPage, lastPage];
        lastRequestedRanges[parentKey] = lastRequestedRange;
        let count = lastPage - firstPage + 1;
        fetch(firstPage * grid.pageSize, count * grid.pageSize);
      }
    }

    grid.dataProvider = function(params, callback) {
      if (params.pageSize != grid.pageSize) {
        throw 'Invalid pageSize';
      }

      let page = params.page;

      if(params.parentItem) {
        let parentUniqueKey = grid.getItemId(params.parentItem);
        let parentIndex = params.parentItem.index;
        if(!treePageCallbacks[parentUniqueKey]) {
          treePageCallbacks[parentUniqueKey] = {};
        }

        let itemCache = grid.getItemCacheByKey(parentUniqueKey);
        if(cache[parentUniqueKey] && cache[parentUniqueKey][page] && itemCache) {
          // workaround: sometimes grid-element gives page index that overflows
          page = Math.min(page, Math.floor(itemCache.size / grid.pageSize));

          callback(cache[parentUniqueKey][page], itemCache.size);
        } else {
          treePageCallbacks[parentUniqueKey][page] = callback;
        }
        grid.fetchPage((firstIndex, size) =>
          grid.$server.setParentRequestedRange(page, firstIndex, size, params.parentItem.key), page, parentUniqueKey, parentIndex);

      } else {
        // workaround: sometimes grid-element gives page index that overflows
        page = Math.min(page, Math.floor(grid.size / grid.pageSize));

        if (cache[root] && cache[root][page]) {
          callback(cache[root][page]);
        } else {
          rootPageCallbacks[page] = callback;
        }

        grid.fetchPage((firstIndex, size) => grid.$server.setRequestedRange(firstIndex, size), page, root);
      }
    }

    const sorterChangeListener = function(event) {
      grid.$server.sortersChanged(grid._sorters.map(function(sorter) {
        return {
          path: sorter.path,
          direction: sorter.direction
        };
      }));
    }
    grid.addEventListener('sorter-changed', sorterChangeListener);

    grid._expandedInstanceChangedCallback = function(inst, value) {
      if (inst.item === undefined) {
        return;
      }
      let parentKey = grid.getItemId(inst.item);
      grid.$server.updateExpandedState(parentKey, value);
      if (value) {
        this.expandItem(inst.item);
      } else {
        delete cache[parentKey];
        let parentCache = grid.getItemCacheByKey(parentKey);
        if(parentCache && parentCache.itemkeyCaches[parentKey]) {
          parentCache.itemkeyCaches[parentKey].items = [];
        }
        delete lastRequestedRanges[parentKey];

        this.collapseItem(inst.item);
      }
    }

    const itemsUpdated = function(items) {
      if (!items || !Array.isArray(items)) {
        throw 'Attempted to call itemsUpdated with an invalid value: ' + JSON.stringify(items);
      }
      const detailsOpenedItems = [];
      let updatedSelectedItem = false;
      for (let i = 0; i < items.length; ++i) {
        const item = items[i];
        if(!item) {
          continue;
        }
        if (item.detailsOpened) {
          detailsOpenedItems.push(item);
        }
        if (selectedKeys[item.key]) {
          selectedKeys[item.key] = item;
          item.selected = true;
          updatedSelectedItem = true;
        }
      }
      grid.detailsOpenedItems = detailsOpenedItems;
      if (updatedSelectedItem) {
        grid.selectedItems = Object.values(selectedKeys);
      }
    }

    const updateGridCache = function(page, parentKey) {
      if((parentKey || root) !== root) {
        const items = cache[parentKey][page];
        let parentCache = grid.getItemCacheByKey(parentKey);
        let _cache = parentCache.itemkeyCaches[parentKey];
        _updateGridCache(page, items,
          treePageCallbacks[parentKey][page],
          _cache);

      } else {
        const items = cache[root][page];
        _updateGridCache(page, items, rootPageCallbacks[page], grid._cache);
      }
    }

    const _updateGridCache = function(page, items, callback, levelcache) {
      // Force update unless there's a callback waiting
      if(!callback) {
        let rangeStart = page * grid.pageSize;
        let rangeEnd = rangeStart + grid.pageSize;
        if (!items) {
          if(levelcache && levelcache.items) {
            for (let idx = rangeStart; idx < rangeEnd; idx++) {
              delete levelcache.items[idx];
            }
          }

        } else {
          if(levelcache && levelcache.items) {
            for (let idx = rangeStart; idx < rangeEnd; idx++) {
              if (levelcache.items[idx]) {
                levelcache.items[idx] = items[idx - rangeStart];
              }
            }
          }
          itemsUpdated(items);
        }
        /**
         * Calls the _assignModels function from GridScrollerElement, that triggers
         * the internal revalidation of the items based on the _cache of the DataProviderMixin.
         */
        grid._assignModels();
      }
    }

	grid.$connector.set = function(index, items, parentKey) {
      if (index % grid.pageSize != 0) {
        throw 'Got new data to index ' + index + ' which is not aligned with the page size of ' + grid.pageSize;
      }
      let pkey = parentKey || root;

      const firstPage = index / grid.pageSize;
      const updatedPageCount = Math.ceil(items.length / grid.pageSize);

      for (let i = 0; i < updatedPageCount; i++) {
        let page = firstPage + i;
        let slice = items.slice(i * grid.pageSize, (i + 1) * grid.pageSize);
        if(!cache[pkey]) {
          cache[pkey] = {};
        }
        cache[pkey][page] = slice;
        for(let j = 0; j < slice.length; j++) {
          let item = slice[j]
          if (item.selected && !isSelectedOnGrid(item)) {
            grid.$connector.doSelection(item);
          } else if (!item.selected && (selectedKeys[item.key] || isSelectedOnGrid(item))) {
            grid.$connector.doDeselection(item);
          }
        }
        updateGridCache(page, pkey);
      }
    };

    const itemToCacheLocation = function(item) {
      let parent = item.parentUniqueKey || root;
      if(cache[parent]) {
        for (let page in cache[parent]) {
          for (let index in cache[parent][page]) {
            if (grid.getItemId(cache[parent][page][index]) === grid.getItemId(item)) {
              return {page: page, index: index, parentKey: parent};
            }
          }
        }
      }
      return null;
    }

    grid.$connector.updateData = function(items) {
      let pagesToUpdate = [];
      for (let i = 0; i < items.length; i++) {
        let cacheLocation = itemToCacheLocation(items[i]);
        if (cacheLocation) {
          cache[cacheLocation.parentKey][cacheLocation.page][cacheLocation.index] = items[i];
          let key = cacheLocation.parentKey+':'+cacheLocation.page;
          if (!pagesToUpdate[key]) {
            pagesToUpdate[key] = {parentKey: cacheLocation.parentKey, page: cacheLocation.page};
          }
        }
      }
      for (let key in pagesToUpdate) {
        let pageToUpdate = pagesToUpdate[key];
        updateGridCache(pageToUpdate.page, pageToUpdate.parentKey);
      }
    };

    grid.$connector.clearExpanded = function() {
      grid.expandedItems = [];
    }

	 grid.$connector.clear = function(index, length, parentKey) {
      let pkey = parentKey || root;
      if (Object.keys(cache[pkey]).length === 0){
        return;
      }
      if (index % grid.pageSize != 0) {
        throw 'Got cleared data for index ' + index + ' which is not aligned with the page size of ' + grid.pageSize;
      }

      let firstPage = Math.floor(index / grid.pageSize);
      let updatedPageCount = Math.ceil(length / grid.pageSize);

      for (let i = 0; i < updatedPageCount; i++) {
        let page = firstPage + i;
        let items = cache[pkey][page];
        for (let j = 0; j < items.length; j++) {
          let item = items[j];
          if (selectedKeys[item.key]) {
            grid.$connector.doDeselection(item);
          }
        }
        delete cache[pkey][page];
        updateGridCache(page, parentKey);
      }
    };

    const isSelectedOnGrid = function(item) {
      const selectedItems = grid.selectedItems;
      for(let i = 0; i < selectedItems; i++) {
        let selectedItem = selectedItems[i];
        if (selectedItem.key === item.key) {
          return true;
        }
      }
      return false;
    }

    grid.$connector.reset = function() {
      grid.size = 0;
      deleteObjectContents(cache);
      deleteObjectContents(grid._cache.items);
      deleteObjectContents(lastRequestedRanges);
      grid._assignModels();
    };

    const deleteObjectContents = function(obj) {
      let props = Object.keys(obj);
      for (let i = 0; i < props.length; i++) {
        delete obj[props[i]];
      }
    }

    grid.$connector.updateSize = function(newSize) {
      grid.size = newSize;
    };

    grid.$connector.updateUniqueItemIdPath = function(path) {
      grid.itemIdPath = path;
    }

    grid.$connector.expandItems = function(items) {
      items.forEach(item => grid.expandItem(item));
    }

    grid.$connector.collapseItems = function(items) {
      items.forEach(item => grid.collapseItem(item));
    }

    grid.$connector.confirmParent = function(id, parentKey, levelSize) {
      if(!treePageCallbacks[parentKey]) {
        return;
      }
      let outstandingRequests = Object.getOwnPropertyNames(treePageCallbacks[parentKey]);
      for(let i = 0; i < outstandingRequests.length; i++) {
        let page = outstandingRequests[i];

        let lastRequestedRange = lastRequestedRanges[parentKey] || [0, 0];
        if((cache[parentKey] && cache[parentKey][page]) || page < lastRequestedRange[0] || page > lastRequestedRange[1]) {
          let callback = treePageCallbacks[parentKey][page];
          delete treePageCallbacks[parentKey][page];
          callback(cache[parentKey][page] || new Array(levelSize), levelSize);
        }
      }
      // Let server know we're done
      grid.$server.confirmParentUpdate(id, parentKey);
    };

    grid.$connector.confirm = function(id) {
      // We're done applying changes from this batch, resolve outstanding
      // callbacks
      let outstandingRequests = Object.getOwnPropertyNames(rootPageCallbacks);
      for(let i = 0; i < outstandingRequests.length; i++) {
        let page = outstandingRequests[i];
        let lastRequestedRange = lastRequestedRanges[root] || [0, 0];
        // Resolve if we have data or if we don't expect to get data
        if ((cache[root] && cache[root][page]) || page < lastRequestedRange[0] || page > lastRequestedRange[1]) {
          let callback = rootPageCallbacks[page];
          delete rootPageCallbacks[page];
          callback(cache[root][page] || new Array(grid.pageSize));
        }
      }

      // Let server know we're done
      grid.$server.confirmUpdate(id);
    }

    grid.$connector.ensureHierarchy = function() {
      for (let parentKey in cache) {
        if(parentKey !== root) {
          delete cache[parentKey];
        }
      }
      deleteObjectContents(lastRequestedRanges);

      grid._cache.itemCaches = {};
      grid._cache.itemkeyCaches = {};

      grid._assignModels();
    }

    grid.$connector.setSelectionMode = function(mode) {
      if ((typeof mode === 'string' || mode instanceof String)
      && validSelectionModes.indexOf(mode) >= 0) {
        selectionMode = mode;
        selectedKeys = {};
      } else {
        throw 'Attempted to set an invalid selection mode';
      }
    }
  }
}
