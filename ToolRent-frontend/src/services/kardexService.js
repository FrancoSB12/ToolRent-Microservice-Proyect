import httpClient from './http-common';

const getAll = () => {
    return httpClient.get('/kardex');
}

const getKardexByToolName = (toolName) => {
    return httpClient.get(`/kardex/tool/${toolName}`);
}

const getKardexByDateRange = (startDate, endDate) => {
    return httpClient.get(`/kardex/by-date?start=${startDate}&end=${endDate}`);
}

export default { getAll, getKardexByToolName, getKardexByDateRange };