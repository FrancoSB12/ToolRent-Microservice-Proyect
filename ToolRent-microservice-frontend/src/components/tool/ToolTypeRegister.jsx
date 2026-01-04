import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import toolTypeService from '../../services/toolTypeService.js';
import '../../styles/Register.css';

const ToolTypeRegister = () => {
    const navigate = useNavigate();

    const [toolTypeData, setToolTypeData] = useState({
        name: '',
        category: '',
        model: '',
        replacementValue: '',
        rentalFee: '',
        damageFee: ''
    });

    const [error, setError] = useState(null);
    
    /* Handle input changes */
    const handleChange = (e) => {
        const { name, value } = e.target;
        setToolTypeData(prevState => ({
            ...prevState,
            [name]: value
        }));
    }

    const handleSubmit = (e) => {
        e.preventDefault();
        setError(null);

        toolTypeService.createType(toolTypeData)
        .then(response => {
            toast.success('Tipo de herramienta registrada con éxito');
            navigate('/tools');
        })
        .catch(err => {
            console.error('Error al registrar:', err);
            if(err.response && err.response.data){
                {/* err.response exists if the backend server responded */}
                if(typeof err.response.data === 'string'){
                    setError(err.response.data);
                } else {
                    setError(err.response.data || "Error interno del servidor. Revisa la consola del backend para más detalles.");
                }
                
            } else if(err.request){
                {/* The error ocurred because there was no response from the backend */}
                setError('No se recibió respuesta del servidor.');
            } else {
                {/* An error ocurred while configuring the request */}
                setError('Ocurrió un error inesperado al registrar la herramienta.');
            }
            
        });
    };

    return (
        <main className="full-page-content">
            <h2 className='form-title'>Registrar Nuevo Tipo de Herramienta</h2>
            <form className='register-form' onSubmit={handleSubmit}>
                {/* Text fields */}
                <div className='form-group'>
                    <input 
                    type='text' 
                    id='name' 
                    name='name' 
                    value={toolTypeData.name} 
                    onChange={handleChange} 
                    placeholder='Nombre' 
                    required
                    />
                </div>

                <div className='form-group'>
                    <input 
                    type='text' 
                    id='category' 
                    name='category' 
                    value={toolTypeData.category} 
                    onChange={handleChange} 
                    placeholder='Categoría' 
                    required 
                    />
                </div>

                <div className='form-group'>
                    <input 
                    type='text' 
                    id='model' 
                    name='model' 
                    value={toolTypeData.model} 
                    onChange={handleChange} 
                    placeholder='Modelo' 
                    required 
                    />
                </div>

                <div className='form-group'>
                    <input 
                    type='number' 
                    id='replacementValue' 
                    name='replacementValue' 
                    value={toolTypeData.replacementValue} 
                    onChange={handleChange} 
                    placeholder='Valor de Reemplazo' 
                    min="0" 
                    required 
                    />
                </div>

                <div className='form-group'>
                    <input 
                    type='number' 
                    id='rentalFee' 
                    name='rentalFee' 
                    value={toolTypeData.rentalFee} 
                    onChange={handleChange} 
                    placeholder='Tarifa de Arriendo' 
                    required 
                    />
                </div>

                <div className='form-group'>
                    <input 
                    type='number' 
                    id='damageFee' 
                    name='damageFee' 
                    value={toolTypeData.damageFee} 
                    onChange={handleChange} 
                    placeholder='Tarifa por Daño' 
                    required 
                    />
                </div>

                {/* Button and Error message display */}
                <div className='form-actions'>
                    {error && <p className='form-error'>{error}</p>}
                    <button type='submit' className='register-submit-btn'>
                        Registrar Tipo de Herramienta
                    </button>
                </div>
            </form>
        </main>
    );
};

export default ToolTypeRegister;