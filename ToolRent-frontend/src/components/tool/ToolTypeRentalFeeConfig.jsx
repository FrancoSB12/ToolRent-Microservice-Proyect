import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import toolTypeService from '../../services/toolTypeService'; 
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import '../../styles/Register.css';

const ToolTypeRentalFeeConfig = () => {
    const [toolTypes, setToolTypes] = useState([]);
    const [selectedToolType, setSelectedToolType] = useState(null);
    const [newFeeAmount, setNewFeeAmount] = useState('');
    const [loading, setLoading] = useState(true);

    const fetchToolTypes = () => {
        setLoading(true);
        toolTypeService.getAllTypes() 
            .then(res => {
                setToolTypes(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Error cargando tipos de herramienta:", err);
                toast.error("Error al cargar la lista de herramientas.");
                setLoading(false);
            });
    };

    useEffect(() => {
        fetchToolTypes();
    }, []);
    
    useEffect(() => {
        if (selectedToolType) {
            setNewFeeAmount(''); 
        }
    }, [selectedToolType]);


    const handleFeeChange = (e) => {
        const value = e.target.value.replace(/[^0-9]/g, '');
        setNewFeeAmount(value);
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!selectedToolType) {
            toast.error("Por favor, selecciona un tipo de herramienta.");
            return;
        }

        const amount = parseInt(newFeeAmount, 10);
        
        if (isNaN(amount) || amount < 0) {
            toast.error("Por favor, ingresa un monto válido y positivo.");
            return;
        }
        const partialUpdate = {
            id: selectedToolType.id,
            rentalFee: amount
        };

        toolTypeService.updateType(selectedToolType.id, partialUpdate) 
            .then(() => {
                toast.success(`¡Tarifa de arriendo para ${selectedToolType.name} actualizada a $${amount}!`);

                //Update local state
                setToolTypes(prevTypes => 
                    prevTypes.map(t => t.id === selectedToolType.id ? { ...t, rentalFee: amount } : t)
                );
                setSelectedToolType(prev => ({ ...prev, rentalFee: amount }));
                
                setNewFeeAmount('');
            })
            .catch(err => {
                const msg = err.response?.data || "Error al actualizar la tarifa.";
                //If the backend returns an error message (e.g., "Invalid fee"), we show it
                toast.error(typeof msg === 'string' ? msg : 'Error interno al actualizar.');
            });
    };

    if (loading) {
        return <main className="full-page-content"><p>Cargando lista de herramientas...</p></main>;
    }

    return (
        <main className="full-page-content">
            <h2 className="form-title">Configuración de Tarifa de Arriendo por Tipo</h2>
            
            <form className="register-form" onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', maxWidth: '500px', margin: '0 auto' }}>
                
                <div className="form-group">
                    <label htmlFor="toolTypeSelect">Seleccionar Tipo de Herramienta:</label>
                    <Autocomplete
                        id="toolTypeSelect"
                        options={toolTypes}
                        getOptionLabel={(option) => `${option.name} (${option.model})`}
                        onChange={(event, newValue) => {
                            setSelectedToolType(newValue);
                        }}
                        value={selectedToolType}
                        sx={{
                            backgroundColor: '#ffffff',
                            borderRadius: '4px',
                            '& .MuiOutlinedInput-root': {
                                padding: '1px!important',
                            },
                            '& .MuiInputLabel-root': {
                                fontSize: '1rem',
                            }
                        }}
                        renderInput={(params) => <TextField {...params} label="Buscar Herramienta" />}
                    />
                </div>

                {selectedToolType && (
                    <>
                        <p style={{textAlign: 'center', fontWeight: 'bold'}}>
                            Tarifa actual para {selectedToolType.name}: ${selectedToolType.rentalFee}
                        </p>
                        <div className="form-group">
                            <label htmlFor="newFee">Nuevo Monto de Tarifa Diaria ($):</label>
                            <input 
                                id="newFee"
                                type="text" 
                                value={newFeeAmount} 
                                onChange={handleFeeChange} 
                                placeholder="Ej: 5000" 
                                required 
                            />
                        </div>

                        <div className="form-actions" style={{ marginTop: '1rem' }}>
                            <button type="submit" className="register-submit-btn" style={{ width: '100%' }}>
                                Guardar Configuración
                            </button>
                        </div>
                    </>
                )}
            </form>
        </main>
    );
};

export default ToolTypeRentalFeeConfig;