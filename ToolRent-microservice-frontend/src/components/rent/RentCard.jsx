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

const formatTime = (time) => {
  if (!time) return '--:--';
  return time.toString().split('.')[0];
};

const RentCard = ({ rent }) => {
  // Verificamos si ya hay una fecha real de devolución para mostrar la sección extra
  const isReturned = !!rent.realReturnDate;

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
                <strong>Atendido por:</strong>
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                    <span>{rent.rentEmployeeNameSnapshot || 'Desconocido'}</span>
                    <span style={{ fontSize: '0.8rem', color: '#888' }}>{rent.rentEmployeeRun}</span>
                </div>
            </div>
            <div className="detail-row">
                <strong>Fecha Est. Devolución:</strong>
                <span>{rent.returnDate}</span>
            </div>
            <div className="detail-row">
                <strong>Multa por día atrasado:</strong>
                <span>${rent.lateReturnFee || 0}</span>
            </div>

            {/* Return section (Only visible if already returned) */}
            {isReturned && (
                <div className="return-section" style={{ borderTop: '1px dashed #ccc', marginTop: '10px', paddingTop: '10px', backgroundColor: '#f9f9f9', padding: '10px', borderRadius: '5px' }}>
                    <div className="detail-row" style={{ color: '#2c3e50' }}>
                        <strong>Fecha Real Devolución:</strong>
                        <span>{rent.realReturnDate} ({formatTime(rent.returnTime) || '--:--'})</span>
                    </div>
                    <div className="detail-row">
                        <strong>Recibido por:</strong>
                        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                             <span>{rent.returnEmployeeNameSnapshot || 'Desconocido'}</span>
                             <span style={{ fontSize: '0.8rem', color: '#888' }}>{rent.returnEmployeeRun}</span>
                        </div>
                    </div>
                </div>
            )}

            <hr style={{ margin: '10px 0', border: '0', borderTop: '1px solid #eee' }}/>

            <div className="detail-row">
                <strong>Total Herramientas:</strong>
                <span>{rent.rentTools ? rent.rentTools.length : 0}</span>
            </div>
            <div className="detail-row" style={{ display: 'block' }}>
                {/* Small list of tools */}
                    {rent.rentTools && (
                    <ul style={{ 
                        fontSize: '0.85rem', 
                        color: '#666', 
                        marginTop: '0.5rem', 
                        fontStyle: 'italic',
                        paddingLeft: '1.2rem',
                        margin: '0.5rem 0'
                    }}>
                        {rent.rentTools.map((rt, index) => (
                            <li key={index} style={{ marginBottom: '4px' }}>
                                {rt.toolNameSnapshot || 'Herramienta desconocida'}
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>

        {/* Footer */}
        <div className="rent-card-footer">
            
        </div>
    </div>
  );
};

export default RentCard;