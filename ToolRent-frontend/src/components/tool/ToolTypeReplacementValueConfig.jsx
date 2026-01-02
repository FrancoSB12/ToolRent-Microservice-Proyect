import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import toolTypeService from '../../services/toolTypeService'; 
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import '../../styles/Register.css'; 

const ToolTypeReplacementValueConfig = () => {
    const [toolTypes, setToolTypes] = useState([]);
    const [selectedToolType, setSelectedToolType] = useState(null);
    const [newValueAmount, setNewValueAmount] = useState('');
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
            setNewValueAmount(''); 
        }
    }, [selectedToolType]);


    const handleValueChange = (e) => {
        const value = e.target.value.replace(/[^0-9]/g, '');
        setNewValueAmount(value);
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!selectedToolType) {
            toast.error("Por favor, selecciona un tipo de herramienta.");
            return;
        }

        const amount = parseInt(newValueAmount, 10);
        
        if (isNaN(amount) || amount < 0) {
            toast.error("Por favor, ingresa un monto válido y positivo.");
            return;
        }

        const partialUpdate = {
            id: selectedToolType.id,
            replacementValue: amount
        };

        toolTypeService.updateType(selectedToolType.id, partialUpdate) 
            .then(() => {
                toast.success(`¡Valor de reposición para ${selectedToolType.name} actualizado a $${amount}!`);
                
                // Actualizar estado local
                setToolTypes(prevTypes => 
                    prevTypes.map(t => t.id === selectedToolType.id ? { ...t, replacementValue: amount } : t)
                );
                setSelectedToolType(prev => ({ ...prev, replacementValue: amount }));
                setNewValueAmount('');
            })
            .catch(err => {
                const msg = err.response?.data || "Error al actualizar el valor.";
                toast.error(typeof msg === 'string' ? msg : 'Error interno.');
            });
    };

    if (loading) {
        return <main className="full-page-content"><p>Cargando lista de herramientas...</p></main>;
    }

    return (
        <main className="full-page-content">
            <h2 className="form-title">Configuración de Valor de Reposición por Tipo</h2>
            
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
                            Valor de reposición actual para {selectedToolType.name}: ${selectedToolType.replacementValue}
                        </p>
                        <div className="form-group">
                            <label htmlFor="newValue">Nuevo Valor de Reposición ($):</label>
                            <input 
                                id="newValue"
                                type="text" 
                                value={newValueAmount} 
                                onChange={handleValueChange} 
                                placeholder="Ej: 150000" 
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

export default ToolTypeReplacementValueConfig;