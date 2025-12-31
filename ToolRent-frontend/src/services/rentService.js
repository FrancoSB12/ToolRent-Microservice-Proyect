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

const getByValidity = validity => {
  return httpClient.get(`/rent/validity/${validity}`);
}

const getMostLoanedTools = () => {
  return httpClient.get('/rent/most-rented-tools');
}

const getCurrentLateFee = () => {
  return httpClient.get('/rent/configuration/late-return-fee');
}

const returnLoan = (id, data) => {
  return httpClient.put(`/rent/return/${id}`, data);
}

const updateLateReturnFee = (id, data) => {
  return httpClient.put(`/rent/late-return-fee/${id}`, data);
}

const updateGlobalLateReturnFee = (amount) => {
  return httpClient.put(`/rent/configuration/late-fee?amount=${amount}`, {});
}

const updateLateStatuses = () => {
  return httpClient.put("/rent/update-late-statuses");
}

export default { create, getAll, getById, getActiveByClientRun, getByStatus, getByValidity, getMostLoanedTools, getCurrentLateFee, returnLoan, updateLateReturnFee, updateGlobalLateReturnFee, updateLateStatuses };