import httpClient from './http-common';

const create = data => {
  return httpClient.post('/rent', data);
}

const getAll = () => {
  return httpClient.get('/rent');
}

const getById = id => {
  return httpClient.get(`/rent/${id}`);
}

const getActiveByClientRun = run => {
  return httpClient.get(`/rent/client/${run}`);
}

const getByStatus = status => {
  return httpClient.get(`/rent/status/${status}`);
}

const getOverdueRents = () => {
  return httpClient.get('/rent/overdue');
}

const getByValidity = validity => {
  return httpClient.get(`/rent/validity/${validity}`);
}

const getToolRanking = () => {
  return httpClient.get(`/rent/stats/ranking`, {
    params: {
      from: FromDate,
      to: toDate
    }
  });
}

const getMostRentedTools = () => {
  return httpClient.get('/rent/most-rented-tools');
}

const returnRent = (id, data) => {
  return httpClient.put(`/rent/return/${id}`, data);
}

const updateLateStatuses = () => {
  return httpClient.put("/rent/update-late-statuses");
}

export default { create, getAll, getById, getActiveByClientRun, getByStatus, getOverdueRents, getByValidity, getToolRanking, getMostRentedTools, returnRent, updateLateStatuses };