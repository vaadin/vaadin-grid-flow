window.Vaadin.Flow.gridConnector = {
  initLazy: function(grid) {
    // Check whether the connector was already initialized for the grid
    if (grid.$connector){
      return;
    }
    const rootPageCallbacks = {};
    const treePageCallbacks = {};
    const cache = {};
    let lastRequestedRange = [0, 0];
    const root = 'null';

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

    grid.dataProvider = function(params, callback) {
      if (params.pageSize != grid.pageSize) {
        throw 'Invalid pageSize';
      }

      const page = params.page;

      if(params.parentItem) {
        let parentUniquKey = grid.getItemId(params.parentItem);
        if(!treePageCallbacks[parentUniquKey]) {
          treePageCallbacks[parentUniquKey] = {};
        }
        treePageCallbacks[parentUniquKey][page] = callback;
        grid.$server.setParentRequestedRange(page, grid.pageSize, params.parentItem.key);
        return;
      }


      if (cache[root] && cache[root][page]) {
        callback(cache[root][page]);
      } else {
        rootPageCallbacks[page] = callback;
      }
      // Determine what to fetch based on scroll position and not only
      // what grid asked for

      // The buffer size could be multiplied by some constant defined by the user,
      // if he needs to reduce the number of items sent to the Grid to improve performance
      // or to increase it to make Grid smoother when scrolling
      let buffer = grid._virtualEnd - grid._virtualStart;

      let firstNeededIndex = Math.max(0, grid._virtualStart + grid._vidxOffset - buffer);
      let lastNeededIndex = Math.min(grid._virtualEnd + grid._vidxOffset + buffer, grid.size);

      let firstNeededPage = Math.min(page, grid._getPageForIndex(firstNeededIndex));
      let lastNeededPage = Math.max(page, grid._getPageForIndex(lastNeededIndex));

      let first = Math.max(0,  firstNeededPage);
      let last = Math.min(lastNeededPage, Math.floor(grid.size / grid.pageSize) + 1);

      if (lastRequestedRange[0] != first || lastRequestedRange[1] != last) {
        lastRequestedRange = [first, last];
        let count = last - first + 1;
        grid.$server.setRequestedRange(first * grid.pageSize, count * grid.pageSize);
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
      grid.$server.updateExpandedState(inst.item.key, value);
      if (value) {
        this.expandItem(inst.item);
      } else {
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

    const updateGridCache = function(page, scaledIndex, parentKey) {
      if((parentKey || root) !== root) {
        const items = cache[parentKey][page];
        _updateGridCache(page, items,
          treePageCallbacks[parentKey][page],
          grid._cache.getCacheAndIndex(scaledIndex).cache);

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
          for (let idx = rangeStart; idx < rangeEnd; idx++) {
            delete levelcache.items[idx];
          }
        } else {
          for (let idx = rangeStart; idx < rangeEnd; idx++) {
            if (levelcache.items[idx]) {
              levelcache.items[idx] = items[idx - rangeStart];
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

    grid.$connector.set = function(index, items) {
      if (index % grid.pageSize != 0) {
        throw 'Got new data to index ' + index + ' which is not aligned with the page size of ' + grid.pageSize;
      }

      const firstPage = index / grid.pageSize;
      const updatedPageCount = Math.ceil(items.length / grid.pageSize);

      for (let i = 0; i < updatedPageCount; i++) {
        let page = firstPage + i;
        let slice = items.slice(i * grid.pageSize, (i + 1) * grid.pageSize);
        if(!cache[root]) {
          cache[root] = {};
        }
        cache[root][page] = slice;
        for(let j = 0; j < slice.length; j++) {
          let item = slice[j]
          if (item.selected && !isSelectedOnGrid(item)) {
            grid.$connector.doSelection(item);
          } else if (!item.selected && (selectedKeys[item.key] || isSelectedOnGrid(item))) {
            grid.$connector.doDeselection(item);
          }
        }
        updateGridCache(page);
      }
    };

    const itemToCacheLocation = function(item) {
      let parent = item.parentUniqueKey || root;
      if(cache[parent]) {
        for (let page in cache[parent]) {
          for (let index in cache[parent][page]) {
            if (grid.getItemId(cache[parent][page][index]) === grid.getItemId(item)) {
              return {page: page, index: index, parentKey: parent, scaledIndex: item.scaledIndex};
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
            pagesToUpdate[key] = {scaledIndex: cacheLocation.scaledIndex, parentKey: cacheLocation.parentKey, page: cacheLocation.page};
          }
        }
      }
      for (let key in pagesToUpdate) {
        let pageToUpdate = pagesToUpdate[key];
        updateGridCache(pageToUpdate.page, pageToUpdate.scaledIndex, pageToUpdate.parentKey);
      }
    };

    grid.$connector.clear = function(index, length) {
      if (Object.keys(cache[root]).length === 0){
        return;
      }
      if (index % grid.pageSize != 0) {
        throw 'Got cleared data for index ' + index + ' which is not aligned with the page size of ' + grid.pageSize;
      }

      let firstPage = Math.floor(index / grid.pageSize);
      let updatedPageCount = Math.ceil(length / grid.pageSize);

      for (let i = 0; i < updatedPageCount; i++) {
        let page = firstPage + i;
        let items = cache[root][page];
        for (let j = 0; j < items.length; j++) {
          let item = items[j];
          if (selectedKeys[item.key]) {
            grid.$connector.doDeselection(item);
          }
        }
        delete cache[root][page];
        updateGridCache(page);
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
      lastRequestedRange = [0, 0];
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

    grid.$connector.confirmTreeLevel = function(parentKey, page, items, levelSize) {
      if(!treePageCallbacks[parentKey]) {
        return;
      }
      let callback = treePageCallbacks[parentKey][page];
      if(callback) {
        let pageitems = items.slice(0, grid.pageSize);
        delete treePageCallbacks[parentKey][page];
        if(!cache[parentKey]) {
          cache[parentKey] = {};
        }
        cache[parentKey][page] = pageitems;
        callback(pageitems, levelSize);
      }
    };

    grid.$connector.confirm = function(id) {
      // We're done applying changes from this batch, resolve outstanding
      // callbacks
      let outstandingRequests = Object.getOwnPropertyNames(rootPageCallbacks);
      for(let i = 0; i < outstandingRequests.length; i++) {
        let page = outstandingRequests[i];
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
