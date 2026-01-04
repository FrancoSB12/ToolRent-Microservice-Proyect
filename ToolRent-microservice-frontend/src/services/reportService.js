import httpClient from './http-common';

const getActiveRents = () => {
    return httpClient.get('/report/active-rents');
};

const getOverdueClients = () => {
    return httpClient.get('/report/overdue-clients');
};

const getRankingReport = (fromDate, toDate) => {
    return httpClient.get('/report/ranking-tools',{
        params: { 
            from: fromDate, 
            to: toDate 
        }
    });
};

export default { getActiveRents, getOverdueClients, getRankingReport };