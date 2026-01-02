import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import feeService from '../../services/feeService';
import '../../styles/Register.css';

const LateFeeConfiguration = () => {
    const navigate = useNavigate();
    
    const [currentFee, setCurrentFee] = useState(null);
    const [newFeeAmount, setNewFeeAmount] = useState('');
    const [loading, setLoading] = useState(true);

    //Load current late fee on component mount
    useEffect(() => {
        feeService.getCurrentLateReturnFee()
            .then(res => {
                setCurrentFee(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Error cargando tarifa actual:", err);
                toast.error("Error al cargar la tarifa actual del sistema.");
                setLoading(false);
            });
    }, []);

    const handleFeeChange = (e) => {
        //If the user types non-numeric characters, they are removed
        const value = e.target.value.replace(/[^0-9]/g, '');
        setNewFeeAmount(value);
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        const amount = parseInt(newFeeAmount, 10);
        
        if (isNaN(amount) || amount < 0) {
            toast.error("Por favor, ingresa un monto válido y positivo.");
            return;
        }

        feeService.updateGlobalLateReturnFee(amount)
            .then(() => {
                toast.success(`¡Multa por atraso actualizada a $${amount}!`);
                setCurrentFee(amount);
                setNewFeeAmount('');

                setTimeout(() => {
                    navigate('/rents');
                }, 1000);
            })
            .catch(err => {
                const msg = err.response?.data || "Error al actualizar la multa.";
                toast.error(typeof msg === 'string' ? msg : 'Error interno.');
            });
    };

    if (loading) {
        return <main className="full-page-content"><p>Cargando configuración...</p></main>;
    }

    return (
        <main className="full-page-content">
            <h2 className="form-title">Configuración de Multa por Atraso</h2>

            <p style={{textAlign: 'center', marginBottom: '1.5rem', fontSize: '1.1rem'}}>
                Multa diaria actual del sistema: 
                <strong> ${currentFee}</strong> (Aplica a futuros préstamos)
            </p>
            
            <form className="register-form" onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', maxWidth: '400px', margin: '0 auto' }}>
                
                <div className="form-group">
                    <label htmlFor="newFee">Nuevo Monto de Multa Diaria ($):</label>
                    <input 
                        id="newFee"
                        type="text"
                        value={newFeeAmount} 
                        onChange={handleFeeChange} 
                        placeholder="Ej: 3500" 
                        required 
                    />
                </div>

                <p style={{fontSize: '0.8rem', color: '#666', marginTop: '5px', textAlign: 'center'}}>
                    * Este cambio afectará la tarifa de todos los préstamos que se registren a partir de ahora.
                </p>

                <div className="form-actions" style={{ marginTop: '1rem' }}>
                    <button 
                        type="submit" 
                        className="register-submit-btn" 
                        style={{ width: '100%' }}>
                        Guardar Configuración
                    </button>
                </div>
            </form>
        </main>
    );
};

export default LateFeeConfiguration;