import httpClient from './http-common';

const create = (data, password) => {
    return httpClient.post('employee', data, {
      params: {
        password: password
      }
    });
}

const getAll = () => {
  return httpClient.get('/employee');
}

const getByRun = run => {
  return httpClient.get(`/employee/${run}`);
}

const getByIsAdmin = () => {
    return httpClient.get('/employee/isAdmin');
}

const update = (run, data) => {
  return httpClient.put(`/employee/${run}`, data);
}

const remove = employeeRun => {
  return httpClient.delete(`/employee/${employeeRun}`);
}

export default { create, getAll, getByRun, getByIsAdmin, update, remove };