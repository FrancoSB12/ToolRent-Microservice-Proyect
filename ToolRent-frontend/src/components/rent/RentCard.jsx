import '../../styles/RentCard.css';

/* Status styles based on validity */
const getRentValidityClass = (validity) => {
  if (!validity) return 'status-default';
  
  const normalized = validity.toUpperCase();
  
  if (normalized === 'PUNTUAL') return 'validity-ontime';
  if (normalized === 'ATRASADO') return 'validity-overdue';
  
  return 'validity-default';
};

const getRentStatusClass = (status) => {
  if (!status) return 'status-default';
  
  const normalized = status.toUpperCase();
  
  if (normalized === 'ACTIVO') return 'status-active';
  if (normalized === 'FINALIZADO') return 'status-finished';
  
  return 'status-default';
};

const RentCard = ({ rent }) => {
  return (
    <div className="rent-card">

        {/* Header: Client information */}
        <div className="rent-card-header">
            <div className="rent-avatar">
                {rent.clientNameSnapshot ? rent.clientNameSnapshot.charAt(0).toUpperCase() : 'C'}
            </div>
            <div className="rent-info">
                <h3>{rent.clientNameSnapshot || 'Cliente Desconocido'}</h3>
                <span className="rent-rut">RUT: {rent.clientRun}</span>
            </div>
            <div className="rent-badges">
                <span className={`rent-status-badge ${getRentStatusClass(rent.status)}`}>
                    {rent.status}
                </span>
                <span className={`rent-validity-badge ${getRentValidityClass(rent.validity)}`}>
                    {rent.validity}
                </span>
            </div>
        </div>

        {/* Body: Rent Details */}
        <div className="rent-card-body">
            <div className="detail-row">
                <strong>ID Arriendo:</strong>
                <span>#{rent.id}</span>
            </div>
            <div className="detail-row">
                <strong>Fecha de Arriendo:</strong>
                <span>{rent.rentDate} ({rent.rentTime})</span>
            </div>
            <div className="detail-row">
                <strong>Fecha de Devolución Estimada:</strong>
                <span>{rent.returnDate}</span>
            </div>
            <div className="detail-row">
                <strong>Multa por día atrasado:</strong>
                <span>${rent.lateReturnFee || 0}</span>
            </div>
            <div className="detail-row">
                <strong>Atendido por:</strong>
                <span>{rent.employeeNameSnapshot || 'Desconocido'}</span>
            </div>
            <div className="detail-row">
                <strong>Total Herramientas:</strong>
                <span>{rent.rentTools ? rent.rentTools.length : 0}</span>
            </div>
            <div className="detail-row">
                {/* Small list of tools */}
                    {rent.rentTools && (
                      <div style={{fontSize: '0.85rem', color: '#666', marginTop: '0.5rem', fontStyle: 'italic'}}>
                        {rent.rentTools.map(rt => rt.toolNameSnapshot || 'Herramienta desconocida').join(', ')}
                      </div>
                    )}
            </div>
        </div>

        {/* Footer with validity (Valid/Overdue) */}
        <div className="rent-card-footer">
            
        </div>
    </div>
  );
};

export default RentCard;