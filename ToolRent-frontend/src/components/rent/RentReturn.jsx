// En: src/components/loans/LoanReturn.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import rentService from '../../services/rentService';
import '../../styles/Register.css'; 
import '../../styles/LoanReturn.css';

const formatRut = (value) => {
  const cleaned = value.replace(/[^0-9kK]/g, '');
  if (cleaned.length <= 1) return cleaned;
  const body = cleaned.slice(0, -1);
  const dv = cleaned.slice(-1).toUpperCase();
  return `${body.replace(/\B(?=(\d{3})+(?!\d))/g, ".")}-${dv}`;
};

const LoanReturn = () => {
  const navigate = useNavigate();
  
  const [clientRun, setClientRun] = useState('');
  const [loans, setLoans] = useState([]);
  const [searched, setSearched] = useState(false);

  const handleSearch = (e) => {
    e.preventDefault();
    if (!clientRun) {
      toast.warning("Ingrese un RUT para buscar.");
      return;
    }

    rentService.getActiveByClientRun(clientRun)
      .then(response => {
        const foundLoans = response.data || [];
        setLoans(foundLoans);
        setSearched(true);
        
        if (foundLoans.length === 0) {
          toast.info("El cliente no tiene préstamos activos pendientes.");
        }
      })
      .catch(err => {
        console.error(err);
        setLoans([]);
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

  const handleProcessReturn = (loanId) => {
    navigate(`/loan/return/${loanId}`);
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

      {/* --- Results --- */}
      {searched && (
        <div className="loans-grid-container">
          {loans.length > 0 ? (
            <div className="loans-grid">
              {loans.map(loan => (
                <div key={loan.id} className="loan-card loan-active">
                  <div className="loan-header">
                    <span className="loan-id">Préstamo #{loan.id}</span>
                    <span className="loan-status status-activo">ACTIVO</span>
                  </div>
                                
                  <div className="loan-body">
                    <p><strong>Fecha Préstamo:</strong> {loan.loanDate}</p>
                    <p><strong>Fecha Límite:</strong> {loan.returnDate}</p>
                    {/* Calculate the number of tools if the list is populated */}
                    <p><strong>Items:</strong> {loan.rentTools ? loan.rentTools.length : 'Cargando...'}</p>
                                    
                    {/* Show tool names (Optional, if the Fetch worked) */}
                    {loan.loanTools && (
                      <div style={{fontSize: '0.85rem', color: '#666', marginTop: '0.5rem'}}>
                        {loan.loanTools.map(lt => lt.toolItem?.toolType?.name).join(', ')}
                      </div>
                    )}
                  </div>

                  <button 
                    className="return-action-btn"
                    onClick={() => handleProcessReturn(loan.id)}
                  >
                    Iniciar Devolución
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <div style={{ textAlign: 'center', marginTop: '2rem', color: '#666' }}>
              <p>No se encontraron préstamos activos para el RUT: <strong>{clientRun}</strong></p>
            </div>
          )}
        </div>
      )}
    </main>
  );
};

export default LoanReturn;