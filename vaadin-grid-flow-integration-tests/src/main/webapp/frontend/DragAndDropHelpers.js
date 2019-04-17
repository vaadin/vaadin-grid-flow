/*
 * Copyright 2000-2019 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
window.dragData = {};

window.fireDragStart = draggable => {
  const event = new Event('dragstart', {
    bubbles: true,
    cancelable: true,
    composed: true
  });
  event.dataTransfer = {
    setDragImage: () => {
    },
    setData: (type, data) => dragData[type] = data
  };
  draggable.dispatchEvent(event);
  return event;
};


window.fireDragEnd = grid => {
  const event = new Event('dragend', {
    bubbles: true,
    cancelable: true,
    composed: true
  });
  grid.$.table.dispatchEvent(event);
  return event;
};

window.fireDrop = (draggable, location) => {

  // First fire drag-over to get the dropLocation
  fireDragOver(draggable, location);

  const event = new Event('drop', {
    bubbles: true,
    cancelable: true,
    composed: true
  });
  event.dataTransfer = {
    getData: type => dragData[type],
    types: Object.keys(dragData)
  };
  draggable.dispatchEvent(event);
  return event;
};


window.fireDragOver = (row, location) => {
  const event = new Event('dragover', {
    bubbles: true,
    cancelable: true,
    composed: true
  });
  const rect = row.getBoundingClientRect();
  if (location === 'on-top') {
    event.clientY = rect.top + rect.height / 2;
  } else if (location === 'above') {
    event.clientY = rect.top;
  } else if (location === 'below') {
    event.clientY = rect.bottom;
  } else if (location === 'under') {
    event.clientY = rect.bottom + rect.height / 2;
  }
  row.dispatchEvent(event);
  return event;
};