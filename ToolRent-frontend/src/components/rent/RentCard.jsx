import '../../styles/LoanCard.css';

/* Status styles based on validity */
const getLoanValidityClass = (validity) => {
  if (!validity) return 'loan-status-default';
  
  const normalized = validity.toUpperCase();
  
  if (normalized === 'VIGENTE' || normalized === 'PUNTUAL') return 'loan-status-active';
  if (normalized === 'ATRASADO') return 'loan-status-restricted';
  if (normalized === 'FINALIZADO') return 'loan-status-default';
  
  return 'loan-status-default';
};

const LoanCard = ({ loan }) => {
  return (
    <div className="loan-card">

        {/* Header: Client information */}
        <div className="loan-card-header">
            <div className="loan-avatar">
            {loan.client?.name ? loan.client.name.charAt(0).toUpperCase() : 'C'}
            </div>
            <div className="loan-info">
            <h3>{loan.client?.name || 'Cliente Desconocido'}</h3>
            <span className="loan-rut">RUT: {loan.client?.run}</span>
            </div>
        </div>

        {/* Body: Loan Details */}
        <div className="loan-card-body">
            <div className="detail-row">
                <strong>ID Préstamo:</strong>
                <span>#{loan.id}</span>
            </div>
            <div className="detail-row">
                <strong>F. Préstamo:</strong>
                <span>{loan.loanDate} ({loan.loanTime})</span>
            </div>
            <div className="detail-row">
                <strong>F. Devolución Estimada:</strong>
                <span>{loan.returnDate}</span>
            </div>
            <div className="detail-row">
                <strong>Multa Diaria:</strong>
                <span>${loan.lateReturnFee || 0}</span>
            </div>
            <div className="detail-row">
                <strong>Estado:</strong>
                <span>{loan.status}</span>
            </div>
        </div>

        {/* Footer with validity (Valid/Overdue) */}
        <div className="loan-card-footer">
            <span className={`loan-status-badge ${getLoanValidityClass(loan.validity)}`}>
                {loan.validity}
            </span>
        </div>
    </div>
  );
};

export default LoanCard;