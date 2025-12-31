import httpClient from './http-common';

const getkardexByToolName = (toolName) => {
    return httpClient.get(`/kardex/tool/${toolName}`);
}

const getAllKardex = () => {
    return httpClient.get('/kardex');
}

const getKardexByDateRange = (startDate, endDate) => {
    return httpClient.get(`/kardex/by-date?start=${startDate}&end=${endDate}`);
}

export default { getkardexByToolName, getAllKardex, getKardexByDateRange };