import httpClient from './http-common';

const getCurrentLateReturnFee = () => {
    return httpClient.get('/fee/current-late-return-fee');
};

const updateGlobalLateReturnFee = (newFee) => {
    const payload = { newLateReturnFee: newFee };
    return httpClient.put('/fee/late-return-fee', payload);
};

const applyLateReturnFeeToClient = (feeData) => {
    const payload = {
        returnDate: feeData.returnDate,
        lateReturnFee: feeData.lateReturnFee,
        client: feeData.client
    };
    return httpClient.put('/fee/apply-late-return-fee', payload);
};

const chargeClientFee = (chargeClientData) => {
    const payload = {
        client: chargeClientData.client,
        fee: chargeClientData.fee
    };
    return httpClient.put('/fee/charge-client', payload);
};

export default { getCurrentLateReturnFee, updateGlobalLateReturnFee, applyLateReturnFeeToClient, chargeClientFee };