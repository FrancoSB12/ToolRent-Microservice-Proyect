import '../../styles/ToolCard.css';

const ToolCard = ({ toolType }) => {
  return (
    <div className="tool-card">
      
      {/*Header (Name, Category and Model) */}
      <div className="tool-card-header">
        <h3>{toolType.name}</h3>
        <div style={{ display: 'flex', gap: '0.5rem', fontSize: '0.9rem', color: '#666' }}>
          <span className="tool-category">{toolType.category}</span>
          <span>•</span>
          <span className="tool-model">{toolType.model}</span>
        </div>
      </div>

      {/*Body (Fees and Stocks) */}
      <div className="tool-card-body">
        
        {/* Row 1: Stocks (Highlighted) */}
        <div className="detail-item" style={{ gridColumn: '1 / -1', marginBottom: '0.5rem' }}>
          <strong>Disponibilidad:</strong>
          <span style={{ fontSize: '1.1rem', color: toolType.availableStock > 0 ? 'var(--success-color)' : 'red' }}>
            {toolType.availableStock} <small style={{ color: '#666' }}>/ {toolType.totalStock} Unidades</small>
          </span>
        </div>

        {/* Row 2: Fees */}
        <div className="detail-item">
          <strong>Arriendo:</strong>
          <span>${toolType.rentalFee}</span>
        </div>
        <div className="detail-item">
          <strong>Multa por Daño:</strong>
          <span>${toolType.damageFee}</span>
        </div>

        {/* Row 3: Replacement Value */}
        <div className="detail-item">
          <strong>Valor de Reposición:</strong>
          <span>${toolType.replacementValue}</span>
        </div>
      </div>
    </div>
  );
};

export default ToolCard;