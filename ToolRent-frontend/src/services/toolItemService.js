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

const updateItem = (id, data) => {
  return httpClient.put(`/inventory/tool-item/${id}`, data);
}

const disableItem = (serialNumber, data) => {
  return httpClient.put(`/inventory/tool-item/disable-tool-item/${serialNumber}`, data);
}

const enableItem = (serialNumber, data) => {
  return httpClient.put(`/inventory/tool-item/enable-tool-item/${serialNumber}`, data);
}

const evaluateItemDamage = (toolId, toolData) => {
  return httpClient.put(`/inventory/tool-item/evaluate-damage/${toolId}`, toolData);
}

const removeItem = id => {
  return httpClient.delete(`/inventory/tool-item/delete/${id}`);
}

export default { createItem, getAllItems, getItemById, getItemBySerialNumber, getFirstAvailableByType, updateItem, disableItem, enableItem, evaluateItemDamage, removeItem };