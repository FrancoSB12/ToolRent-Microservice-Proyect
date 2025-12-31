import httpClient from './http-common';

const create = data => {
  return httpClient.post('/client', data);
}

const getAll = () => {
  return httpClient.get('/client');
}

const getByRun = run => {
  return httpClient.get(`/client/${run}`);
}

const getByStatus = status => {
  return httpClient.get(`/client/status/${status}`);
}

const update = (run, data) => {
  return httpClient.put(`/client/${run}`, data);
}

const remove = run => {
  return httpClient.delete(`/client/${run}`);
}

export default { create, getAll, getByRun, getByStatus, update, remove };