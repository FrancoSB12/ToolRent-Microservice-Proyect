import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useKeycloak } from '@react-keycloak/web';
import rentService from '../../services/rentService';
import toolItemService from '../../services/toolItemService';
import toolTypeService from '../../services/toolTypeService';
import TextField from '@mui/material/TextField';
import Autocomplete from '@mui/material/Autocomplete';
import '../../styles/Register.css'; 
import { renderToPipeableStream } from 'react-dom/server';

const formatRun = (run) => {
    //First, everything that isn't a number or K is cleared
    const cleanedRun = run.replace(/[^0-9kK]/g, '');

    //The minimum length is validated
    if(cleanedRun.length <= 1) return cleanedRun;

    //The body and the verifier digit are separated
    const body = cleanedRun.slice(0, -1);
    const dv = cleanedRun.slice(-1).toUpperCase();

    //Format the body with dots (magic regex)
    const formattedBody = body.replace(/\B(?=(\d{3})+(?!\d))/g, ".");

    //Return with hyphen
    return `${formattedBody}-${dv}`;
}

const RentRegister = () => {
    const navigate = useNavigate();
    const { keycloak } = useKeycloak();

    const [clientRun, setClientRun] = useState('');
    const [returnDate, setReturnDate] = useState('');
    const [addedTools, setAddedTools] = useState([]);
    
    //Tool type related states
    const [toolTypes, setToolTypes] = useState([]);
    const [selectedType, setSelectedType] = useState(null);

    //Load the tool type list on mount
    useEffect(() => {
        toolTypeService.getAllTypes()
            .then(res => setToolTypes(res.data))
            .catch(err => console.error("Error cargando los tipos de herramientas", err));
    }, []);

    //Logic for "Add Tool Automatically"
    const handleAddTool = async () => {
        if (!selectedType) {
            toast.warning("Selecciona un tipo de herramienta primero.");
            return;
        }

        try {
            //Request from the backend an available unit of this type
            const response = await toolItemService.getFirstAvailableByType(selectedType.id);
            const toolToAdd = response.data;

            //Validate if already have it in the visual list (to avoid adding it twice)
            if (addedTools.some(t => t.id === toolToAdd.id)) {
                toast.warning(`Ya agregaste una unidad de ${selectedType.name} (Serie: ${toolToAdd.serialNumber}). Agrega otra si necesitas más.`);
                return;
            }

            //Add to the list
            setAddedTools([...addedTools, toolToAdd]);
            toast.success(`Agregada: ${toolToAdd.serialNumber}`);
            setSelectedType(null);

        } catch (error) {
            //If the backend responds with 404, it means there is no available/good stock
            if (error.response && error.response.status === 404) {
                toast.error(`No hay unidades disponibles o en buen estado de ${selectedType.name}.`);
            } else {
                toast.error("Error al buscar herramienta.");
            }
        }
    };

    const handleRunChange = (e) => {
        const val = e.target.value;
        setClientRun(formatRun(val));
    };

    const handleRemoveTool = (idToRemove) => {
        setAddedTools(addedTools.filter(t => t.id !== idToRemove));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (addedTools.length === 0) {
            toast.error('Debes agregar al menos una herramienta.');
            return;
        }

        const employeeRun = keycloak.tokenParsed?.preferred_username;

        if (!employeeRun) {
            toast.error("No se pudo identificar al empleado. Inicie sesión nuevamente.");
            return;
        }

        const rentEntity = {
            clientRun: clientRun,
            employeeRun: employeeRun,
            returnDate: returnDate,
            rentDate: new Date().toISOString().split('T')[0],
            rentTime: new Date().toTimeString().split(' ')[0],
            rentTools: addedTools.map(tool => ({ 
                toolItemId: tool.id,
                toolTypeId: tool.toolType.id
            }))
        };

        rentService.create(rentEntity)
            .then(() => {
                toast.success('¡Arriendo registrado!');
                navigate('/rents');
            })
            .catch(err => {
                const msg = err.response?.data || "Error al registrar.";
                toast.error(typeof msg === 'string' ? msg : 'Error interno.');
            });
    };

    return (
        <main className="full-page-content">
            <h2 className="form-title">Registrar Nuevo Préstamo</h2>
            
            <form className="register-form" onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                
                {/* Client and Return Date Inputs */}
                 <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
                    <div className="form-group">
                        <input 
                            type="text" 
                            value={clientRun} 
                            onChange={handleRunChange} 
                            placeholder="RUT del Cliente" 
                            required 
                            maxLength={12}
                        />
                    </div>
                    <div className="form-group">
                        <input 
                            type="date" 
                            value={returnDate} 
                            onChange={(e) => setReturnDate(e.target.value)} 
                            required 
                            min={new Date().toISOString().split('T')[0]} 
                        />
                    </div>
                </div>

                <hr style={{ width: '100%', border: '0', borderTop: '1px solid #eee', margin: '1rem 0' }} />

                {/* Tool type selector (AUTOCOMPLETE) */}
                <h3 style={{ fontSize: '1.1rem', margin: '0', color: '#333' }}>Agregar Herramientas</h3>
                
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
                    
                    <div style={{ flexGrow: 1 }}>
                        <Autocomplete
                            id="tool-type-select"
                            options={toolTypes}
                            getOptionLabel={(option) => `${option.name} (${option.model})`}
                            value={selectedType}
                            onChange={(event, newValue) => setSelectedType(newValue)}
                            renderInput={(params) => (
                                <TextField 
                                    {...params} 
                                    placeholder="Buscar Tipo de Herramienta (Nombre o Modelo)"
                                    variant="standard"
                                    slotProps={{
                                        input: {
                                            ...params.InputProps,
                                            disableUnderline: true, 
                                            style: { 
                                                padding: '0.5rem 1rem',
                                                backgroundColor: '#f4f4f4', 
                                                borderRadius: '8px',
                                                border: '1px solid #ccc', 
                                                height: '56px',
                                                display: 'flex', alignItems: 'center'
                                            } 
                                        }
                                    }}
                                    style={{ width: '100%' }} 
                                />
                            )}
                        />
                         <p style={{fontSize: '0.8rem', color: '#666', marginTop: '5px', marginLeft: '5px'}}>
                            * El sistema seleccionará automáticamente una unidad disponible.
                        </p>
                    </div>

                    <button 
                        type="button" 
                        onClick={handleAddTool}
                        className="register-submit-btn"
                        style={{ width: 'auto', padding: '0 2rem', height: '56px', minWidth: '120px' }}
                    >
                        Agregar
                    </button>
                </div>

                {/* Added tools list */}
                <div style={{ background: '#f9f9f9', borderRadius: '8px', padding: '1rem', minHeight: '100px' }}>
                    {addedTools.length === 0 ? (
                        <p style={{ color: '#999', textAlign: 'center', fontStyle: 'italic' }}>
                            Lista vacía. Seleccione un tipo y pulse Agregar.
                        </p>
                    ) : (
                        <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                            {addedTools.map((tool) => (
                                <li key={tool.id} style={{ 
                                    background: 'white', border: '1px solid #eee', borderRadius: '4px', 
                                    padding: '0.8rem', marginBottom: '0.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center'
                                }}>
                                    <div>
                                        {/* It Shows the Tool Type Name and Assigned Unit Serial Number */}
                                        <strong>{tool.toolType?.name}</strong> 
                                        <span style={{ color: '#666', marginLeft: '10px' }}>SN: {tool.serialNumber}</span>
                                    </div>
                                    <button type="button" onClick={() => handleRemoveTool(tool.id)} style={{ color: 'red', background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.2rem' }}>
                                        &times;
                                    </button>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                <div className="form-actions" style={{ marginTop: '1rem' }}>
                    <button type="submit" className="register-submit-btn" style={{ width: '100%' }}>
                        Confirmar Préstamo
                    </button>
                </div>
            </form>
        </main>
    );
};

export default RentRegister;