import React, { useState } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { toast } from 'react-toastify';
import '../../styles/ToolCard.css';
import toolTypeService from '../../services/toolTypeService.js';

const ToolCard = ({ toolType, onUpdate }) => {
  const { keycloak } = useKeycloak();
  const [isEditing, setIsEditing] = useState(false);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({
    name: toolType.name,
    category: toolType.category,
    model: toolType.model
  });

  const isAdmin = keycloak?.authenticated && keycloak?.hasRealmRole('Admin');

  //Handle input changes
  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };
  
  //Save changes
  const handleSave = async (e) => {
    e.stopPropagation();
    
    //It's built the payload
    const payload = {
      ...toolType,
      name: formData.name,
      category: formData.category,
      model: formData.model
    };

    try {
      await toolTypeService.updateType(toolType.id, payload);

      //SUCCESS: Show the green toast
      toast.success('¡Herramienta actualizada con éxito!');
      
      //Close the editing mode
      setIsEditing(false);
      
      //Notify the parent to reload the list
      if (onUpdate) onUpdate();

    } catch (err) {
      console.error('Error al actualizar:', err);

      //Logic to determine which message to show in the red Toast
      let errorMessage = 'Ocurrió un error inesperado.';

      if (err.response && err.response.data) {
        // The server responded with an error (e.g., 400, 500)
        if (typeof err.response.data === 'string') {
          errorMessage = err.response.data;
        } else {
          // If the backend returns a JSON error object, try to show a generic or specific message
          errorMessage = err.response.data.message || "Internal server error.";
        }
      } else if (err.request) {
        // The request was made but no response was received (server down)
        errorMessage = 'No se recibió respuesta del servidor. Verifica tu conexión.';
      }

      //ERROR: it's showed the red toast with the processed message
      toast.error(errorMessage);
    }
  };

  const handleCancel = (e) => {
    e.stopPropagation();
    // Revert changes to the original state if canceled
    setFormData({
      name: toolType.name,
      category: toolType.category,
      model: toolType.model
    });
    setIsEditing(false);
  };

  return (
    <div className= {`tool-card ${isAdmin ? 'admin-mode' : ''}`} onClick={isAdmin ? () => setIsEditing(true) : undefined}>
      
      {/* Normal view (It's visually hidden if we are editing, or stays underneath) --- */}
      <div style={{ visibility: isEditing ? 'hidden' : 'visible' }}>
        <div className="tool-card-header">
          <h3>{toolType.name}</h3>
          <div style={{ display: 'flex', gap: '0.5rem', fontSize: '0.9rem', color: '#666' }}>
            <span className="tool-category">{toolType.category}</span>
            <span>•</span>
            <span className="tool-model">{toolType.model}</span>
          </div>
        </div>

        <div className="tool-card-body">
          <div className="detail-item" style={{ gridColumn: '1 / -1', marginBottom: '0.5rem' }}>
            <strong>Disponibilidad:</strong>
            <span style={{ fontSize: '1.1rem', color: toolType.availableStock > 0 ? 'var(--success-color)' : 'red' }}>
              {toolType.availableStock} <small style={{ color: '#666' }}>/ {toolType.totalStock}</small>
            </span>
          </div>
          <div className="detail-item">
            <strong>Arriendo:</strong> <span>${toolType.rentalFee}</span>
          </div>
          <div className="detail-item">
            <strong>Multa:</strong> <span>${toolType.damageFee}</span>
          </div>
          <div className="detail-item">
            <strong>Reposición:</strong> <span>${toolType.replacementValue}</span>
          </div>
        </div>
      </div>

      {/* Edition overlay*/}
      {isEditing && (
        <div className="card-edit-overlay" onClick={(e) => e.stopPropagation()}>
          <h4>Editar Información</h4>
          
          <div className="edit-group">
            <input 
              name="name" 
              value={formData.name} 
              onChange={handleChange} 
              placeholder="Nombre"
              autoFocus
            />
          </div>
          
          <div className="edit-row">
            <input 
              name="category" 
              value={formData.category} 
              onChange={handleChange} 
              placeholder="Categoría"
            />
            <input 
              name="model" 
              value={formData.model} 
              onChange={handleChange} 
              placeholder="Modelo"
            />
          </div>

          <div className="edit-actions">
            <button className="btn-mini-cancel" onClick={handleCancel}>Cancelar</button>
            <button className="btn-mini-save" onClick={handleSave}>Guardar</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ToolCard