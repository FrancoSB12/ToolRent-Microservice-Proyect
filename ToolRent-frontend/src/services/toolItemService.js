import httpClient from './http-common';

const createItem = data => {
  return httpClient.post('/inventory/tool-item', data);
}

const getAllItems = () => {
  return httpClient.get('/inventory/tool-item');
}

const getItemById = id => {
  return httpClient.get(`/inventory/tool-item/${id}`);
}

const getItemBySerialNumber = serialNumber => {
  return httpClient.get(`/inventory/tool-item/serial-number/${serialNumber}`);
}

const getFirstAvailableByType = (toolTypeId) => {
  return httpClient.get(`/inventory/tool-item/type/${toolTypeId}`);
}

const getToolTypesByToolItemId = (toolItemIds) => {
  return httpClient.post('/inventory/tool-item/get-tool-types', toolItemIds);
}

const updateItem = (id, data) => {
  return httpClient.put(`/inventory/tool-item/${id}`, data);
}

const enableItem = (serialNumber) => {
  return httpClient.put(`/inventory/tool-item/enable-tool-item/${serialNumber}`);
}

const disableItem = (serialNumber, data) => {
  return httpClient.put(`/inventory/tool-item/disable-tool-item/${serialNumber}`, data);
}

const evaluateItemDamage = (toolId, toolData) => {
  return httpClient.put(`/inventory/tool-item/evaluate-damage/${toolId}`, toolData);
}

const removeItem = id => {
  return httpClient.delete(`/inventory/tool-item/delete/${id}`);
}

export default { createItem, getAllItems, getItemById, getItemBySerialNumber, getFirstAvailableByType, getToolTypesByToolItemId, updateItem, disableItem, enableItem, evaluateItemDamage, removeItem };