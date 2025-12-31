import '../../styles/ClientCard.css';

/* Change the way client status is displayed */
const getClientStatusClass = (status) => {
  if (!status) return 'cli-status-default';
  
  const normalized = status.toUpperCase();
  
  if (normalized === 'ACTIVO') return 'cli-status-activo';
  if (normalized === 'RESTRINGIDO') return 'cli-status-restringido';
  
  return 'cli-status-default';
};

const ClientCard = ({ client }) => {
  return (
    <div className="client-card">
        {/* Header with initials */}
        <div className="client-card-header">
            <div className="client-avatar">
            {client.name ? client.name.charAt(0).toUpperCase() : 'U'}
            </div>
            <div className="client-info">
            <h3>{client.name}</h3>
            <h3>{client.surname}</h3>
            <span className="client-rut">{client.rut}</span>
            </div>
        </div>

        {/* Body with details */}
        <div className="client-card-body">
            <div className="detail-row">
                <strong>Email:</strong>
                <span>{client.email || 'Sin email'}</span>
            </div>
            <div className="detail-row">
                <strong>Celular:</strong>
                <span>{client.cellphone || 'Sin celular'}</span>
            </div>
            <div className="detail-row">
                <strong>Deuda:</strong>
                <span>{client.debt || 'Sin deuda'}</span>
            </div>
            <div className="detail-row">
                <strong>Herramientas en posesion:</strong>
                <span>{client.borrowedTools || 'Sin herramientas en posesión'}</span>
            </div>
        </div>

        {/* 3. Footer with badge */}
        <div className="client-card-footer">
            <span className={`cli-status-badge ${getClientStatusClass(client.status)}`}>
                {client.status}
            </span>
        </div>
    </div>
  );
};

export default ClientCard;