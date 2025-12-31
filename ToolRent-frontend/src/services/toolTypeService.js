import httpClient from './http-common';

const createType = data => {
    return httpClient.post('/inventory/tool-type', data);
}

const getAllTypes = () => {
    return httpClient.get('/inventory/tool-type');
}

const getTypeById = id => {
    return httpClient.get(`/inventory/tool-type/${id}`);
}

const getTypeByName = name => {
    return httpClient.get(`/inventory/tool-type/name/${name}`);
}

const updateType = (id, newValue) => {
    const toolTypeUpdate = {
        id: id,
        replacementValue: newValue
    };

    return httpClient.put(`/inventory/tool-type/${id}`, toolTypeUpdate);
};

const deleteType = id => {
    return httpClient.delete(`/inventory/tool-type/delete/${id}`);
}

export default { createType, getAllTypes, getTypeById, getTypeByName, updateType, deleteType };