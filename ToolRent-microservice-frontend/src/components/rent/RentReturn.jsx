import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import rentService from '../../services/rentService';
import '../../styles/Register.css'; 
import '../../styles/RentReturn.css';

const formatRut = (value) => {
  const cleaned = value.replace(/[^0-9kK]/g, '');
  if (cleaned.length <= 1) return cleaned;
  const body = cleaned.slice(0, -1);
  const dv = cleaned.slice(-1).toUpperCase();
  return `${body.replace(/\B(?=(\d{3})+(?!\d))/g, ".")}-${dv}`;
};

const RentReturn = () => {
  const navigate = useNavigate();
  
  const [clientRun, setClientRun] = useState('');
  const [rents, setRents] = useState([]);
  const [searched, setSearched] = useState(false);

  const handleSearch = (e) => {
    e.preventDefault();
    if (!clientRun) {
      toast.warning("Ingrese un RUT para buscar.");
      return;
    }

    rentService.getActiveByClientRun(clientRun)
      .then(response => {
        const foundRents = response.data || [];
        setRents(foundRents);
        setSearched(true);
        
        if (foundRents.length === 0) {
          toast.info("El cliente no tiene arriendos activos pendientes.");
        }
      })
      .catch(err => {
        console.error(err);
        setRents([]);
        setSearched(true);

        if (err.response && err.response.status === 404) {
          toast.error("Cliente no encontrado en la base de datos.");
        } else if (err.response && err.response.status === 204) {
          toast.info("El cliente no tiene préstamos activos.");
        } else {
          toast.error("Error al buscar los préstamos.");
        }
      });
  };

  const handleProcessReturn = (rentId) => {
    navigate(`/rent/return/${rentId}`);
  };

  return (
    <main className="full-page-content">
      <h2 className="form-title">Realizar Devolución</h2>

      {/* --- Search bar --- */}
      <div style={{ maxWidth: '700px', margin: '0 auto 3rem auto' }}>
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <div className="form-group" style={{ flexGrow: 1, marginBottom: 0 }}>
            <input 
              type="text" 
              value={clientRun} 
              onChange={(e) => setClientRun(formatRut(e.target.value))} 
              placeholder="Ingrese RUN del Cliente" 
              required 
              maxLength={12}
            />
          </div>
          <button type="submit" className="register-submit-btn" style={{ width: 'auto', padding: '0 2rem', height: '56px' }}>
            Buscar
          </button>
        </form>
      </div>

      {/* --- Results in grid --- */}
      {searched && (
        <>
          {rents.length > 0 ? (
            <div className="rents-grid">
              {rents.map(rent => (
                <div key={rent.id} className="rent-card rent-active">
                  
                  {/* Header: Title on the left, Validity on the right */}
                  <div className="rent-header">
                    <span className="rent-id">Arriendo #{rent.id}</span>
                    <span className={`rent-validity ${rent.validity === 'Puntual' ? 'validity-ontime' : 'validity-overdue'}`}> {rent.validity}</span>
                  </div>
                                
                  {/* Body: Details aligned to the left */}
                  <div className="rent-body">
                    <p><strong>Fecha Arriendo:</strong> {rent.rentDate}</p>
                    <p><strong>Fecha Límite:</strong> {rent.returnDate}</p>
                    <p><strong>Herramientas:</strong> {rent.rentTools ? rent.rentTools.length : 0}</p>
                                    
                    {/* Small list of tools */}
                    {rent.rentTools && (
                      <div style={{fontSize: '0.85rem', color: '#666', marginTop: '0.5rem', fontStyle: 'italic'}}>
                        {rent.rentTools.map(rt => rt.toolNameSnapshot || 'Herramienta desconocida').join(', ')}
                      </div>
                    )}
                  </div>

                  <button 
                    className="return-action-btn"
                    onClick={() => handleProcessReturn(rent.id)}
                  >
                    Iniciar Devolución
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <div style={{ textAlign: 'center', marginTop: '2rem', color: '#fff', fontSize: '1.2rem' }}>
              <p>No se encontraron arriendos activos para el RUT: <strong>{clientRun}</strong></p>
            </div>
          )}
        </>
      )}
    </main>
  );
};

export default RentReturn;