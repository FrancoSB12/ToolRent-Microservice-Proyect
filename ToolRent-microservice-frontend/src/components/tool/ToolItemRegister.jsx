import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import toolTypeService from '../../services/toolTypeService.js';
import toolItemService from '../../services/toolItemService.js';
import TextField from '@mui/material/TextField';
import Autocomplete from '@mui/material/Autocomplete';
import '../../styles/Register.css';

/* The status of the tools are defined */
const STATUS_OPTIONS = [
  { value: 'DISPONIBLE', label: 'Disponible' },
  { value: 'EN_REPARACION', label: 'En Reparación' },
];

/* The damage levels of the tools are defined */

const DAMAGE_LEVEL_OPTIONS = [
  { value: 'NO_DANADA', label: 'No dañada' },
  { value: 'LEVEMENTE_DANADA', label: 'Levemente dañada' },
  { value: 'DANADA', label: 'Dañada' },
  { value: 'GRAVEMENTE_DANADA', label: 'Gravemente dañada' },
];

const ToolItemRegister = () => {
    const navigate = useNavigate();

    const [toolTypeData, setToolTypeData] = useState([]);   //Tool type list for the autocomplete
    const [selectedToolType, setSelectedToolType] = useState(null); //Selected tool type from the autocomplete

    const [error, setError] = useState(null);

    const [toolItemData, setToolItemData] = useState({
        serialNumber: '',
        status: '',
        damageLevel: '',
    });

    /* Fetch tool types for the autocomplete */
    useEffect(() => {
        toolTypeService.getAllTypes()
        .then(response => {
            setToolTypeData(response.data);
        })
        .catch(error => {
            console.error('Error al cargar los tipos de herramienta:', error);
            toast.error('Error al cargar los tipos de herramienta. Revisa la consola para más detalles.');
        });
    }, []);
    
    /* Handle input changes */
    const handleChange = (e) => {
        const { name, value } = e.target;
        setToolItemData(prevState => ({
            ...prevState,
            [name]: value
        }));
    }

    const handleSubmit = (e) => {
        e.preventDefault();
        setError(null);

        /* Basic validations */
        if(!selectedToolType){
            toast.error('Por favor, seleccione un tipo de herramienta.');
            return;
        }
        if(!toolItemData.serialNumber){
            toast.error('El número de serie es obligatorio.');
            return;
        }

        /* The DTO is built */
        const dataToSend = {
            serialNumber: toolItemData.serialNumber,
            status: toolItemData.status,
            damageLevel: toolItemData.damageLevel,
            toolType: { id: selectedToolType.id }
        };

        toolItemService.createItem(dataToSend)
        .then(response => {
            toast.success('Unidad de herramienta registrada con éxito');
            navigate('/tools/tool-items');
        })
        .catch(err => {
            console.error('Error al registrar:', err);
            if(err.response && err.response.data){
                {/* err.response exists if the backend server responded */}
                if(typeof err.response.data === 'string'){
                    toast.error(err.response.data);
                } else {
                    toast.error(err.response.data || "Error interno del servidor. Revisa la consola del backend para más detalles.");
                }
                
            } else if(err.request){
                {/* The error ocurred because there was no response from the backend */}
                toast.error('No se recibió respuesta del servidor.');
            } else {
                {/* An error ocurred while configuring the request */}
                toast.error('Ocurrió un error inesperado al registrar la herramienta.');
            }
            
        });
    };

    return (
        <main className="full-page-content">
            <h2 className='form-title'>Registrar Nueva Unidad de Herramienta</h2>
            <form className='register-form' onSubmit={handleSubmit}>
                {/* Autocomplete for tool types */}
                <div className='form-group' style={{ gridColumn: '1 / -1' }}>
                    <Autocomplete
                        id="tool-type-select"
                        options={toolTypeData}
                        getOptionLabel={(option) => `${option.name} (${option.model})`}
                        value={selectedToolType}
                        onChange={(event, newValue) => {
                            setSelectedToolType(newValue);
                        }}
                        renderInput={(params) => (
                            <TextField 
                                {...params} 
                                placeholder="Seleccione un Tipo de Herramienta"
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
                                            boxSizing: 'border-box',
                                            display: 'flex',
                                            alignItems: 'center'
                                        } 
                                    }
                                }}
                                style={{ width: '100%' }} 
                            />
                        )}

                        filterOptions={(options, state) => {
                            const inputValue = state.inputValue.toLowerCase();
                            return options.filter(opt => 
                                opt.name.toLowerCase().includes(inputValue) || 
                                opt.model.toLowerCase().includes(inputValue)
                            );
                        }}
                    />
                </div>

                {/* Text fields */}
                <div className='form-group'>
                    <input 
                    type='text' 
                    id='serialNumber' 
                    name='serialNumber' 
                    value={toolItemData.serialNumber} 
                    onChange={handleChange} 
                    placeholder='Número de Serie' 
                    required
                    />
                </div>

                {/* Selection fields */}
                <div className='form-group'>
                    <select 
                    id='status' 
                    name='status' 
                    value={toolItemData.status} 
                    onChange={handleChange} 
                    required
                    style={{ color: toolItemData.status === '' ? '#888' : '#000' }}
                    >
                        <option value="" disabled hidden>Estado</option>
                        {STATUS_OPTIONS.map(option => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                </div>

                <div className='form-group'>
                    <select 
                    id='damageLevel' 
                    name='damageLevel' 
                    value={toolItemData.damageLevel}
                    onChange={handleChange} 
                    required
                    style={{ color: toolItemData.damageLevel === '' ? '#888' : '#000' }}
                    >
                        <option value="" disabled hidden>Nivel de Daño</option>
                        {DAMAGE_LEVEL_OPTIONS.map(option => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                </div>

                {/* Button and Error message display */}
                <div className='form-actions'>
                    {error && <p className='form-error'>{error}</p>}
                    <button type='submit' className='register-submit-btn'>
                        Registrar Unidad de Herramienta
                    </button>
                </div>
            </form>
        </main>
    );
};

export default ToolItemRegister;