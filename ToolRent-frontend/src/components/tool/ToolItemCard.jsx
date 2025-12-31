import '../../styles/ToolCard.css'; 

const getStatusClass = (status) => {
  switch (status) {
    case 'DISPONIBLE': return 'status-disponible';
    case 'PRESTADA': return 'status-prestada';
    case 'EN_REPARACION': return 'status-en-reparacion';
    case 'DADA_DE_BAJA': return 'status-dada-de-baja';
    default: return 'status-default';
  }
};

const getDamageClass = (damage) => {
  switch (damage) {
    case 'NO_DANADA': return 'damage-no-danada';
    case 'LEVEMENTE_DANADA': return 'damage-levemente-danada';
    case 'DANADA': return 'damage-danada';
    case 'GRAVEMENTE_DANADA': return 'damage-gravemente-danada';
    case 'IRREPARABLE': return 'damage-irreparable';
    case 'DESUSO': return 'damage-desuso';
    case 'EN_EVALUACION': return 'damage-en-evaluacion';
    default: return 'damage-default';
  }
};

/* Change the way statuses and damage levels are displayed */
const formatBadgeText = (text) => {
  if (!text) return '';
  
  return text
    .replace(/_/g, ' ')   //Replace _ with " "
    .toLowerCase()
    .replace('danada', 'dañada') //Fix special character
    .split(' ')           //Split into words -> ['dada', 'de', 'baja']
    .map(word => word.charAt(0).toUpperCase() + word.slice(1)) //Capitalize first letter -> ['Dada', 'De', 'Baja']
    .join(' ');           //Join back to string -> "Dada De Baja"
};

const ToolItemCard = ({ toolItem }) => {
  return (
    <div className="tool-card">
      {/* 1. Header (Name and Category) */}
      <div className="tool-card-header">
        <h3>{toolItem.toolType?.name || 'Nombre no disponible'}</h3>
      </div>

      {/* 2. Body (serial number) */}
      <div className="tool-card-body">
        <div className="detail-item">
          <strong>Número de Serie: </strong>
          <span>{toolItem.serialNumber || 'N/A'}</span>
        </div>
      </div>

      {/* 3. Footer (Status and damage) */}
      <div className="tool-card-footer">
        <span className={`status-badge ${getStatusClass(toolItem.status)}`}>
          {formatBadgeText(toolItem.status)}
        </span>
        <span className={`status-badge ${getDamageClass(toolItem.damageLevel)}`}>
          {formatBadgeText(toolItem.damageLevel)}
        </span>
      </div>
    </div>
  );
};

export default ToolItemCard;