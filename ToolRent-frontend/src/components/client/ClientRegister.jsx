import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import clientService from '../../services/clientService.js';
import '../../styles/Register.css';

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

const ClientsRegister = () => {
    const navigate = useNavigate();

    const [clientData, setClientData] = useState({
        run: '',
        name: '',
        surname: '',
        email: '',
        cellphone: '',
    });

    const [error, setError] = useState(null);
    
    /* Handle input changes */
    const handleChange = (e) => {
        const { name, value } = e.target;

        if(name === 'run'){
            setClientData(prevState => ({
                ...prevState,
                [name]: formatRun(value)
            }));
            return;
        }
        setClientData(prevState => ({
                ...prevState,
                [name]: value
            }));
    }

    const handleSubmit = (e) => {
        e.preventDefault();
        setError(null);

        clientService.create(clientData)
        .then(response => {
            toast.success('Cliente registrado con éxito');
            navigate('/clients');
        })
        .catch(err => {
            console.error('Error al registrar:', err);
            if(err.response && err.response.data){
                {/* err.response exists if the backend server responded */}
                if(typeof err.response.data === 'string'){
                    setError(err.response.data);
                } else {
                    setError(err.response.data.message || 'Ocurrió un error al registrar al cliente.');
                }
            } else if(err.request){
                {/* The error ocurred because there was no response from the backend */}
                setError('No se recibió respuesta del servidor.');
            } else {
                {/* An error ocurred while configuring the request */}
                setError('Ocurrió un error inesperado al registrar al cliente.');
            }
            
        });
    };

    return (
        <main className="full-page-content">
            <h2 className='form-title'>Registrar Nuevo Cliente</h2>
            <form className='register-form' onSubmit={handleSubmit}>
                {/* Text fields */}
                <div className='form-group'>
                    <input 
                    type='text' 
                    id='run' 
                    name='run' 
                    value={clientData.run} 
                    onChange={handleChange} 
                    placeholder='Run' 
                    required
                    />
                </div>

                <div className='form-group'>
                    <input 
                    type='text' 
                    id='name' 
                    name='name' 
                    value={clientData.name} 
                    onChange={handleChange} 
                    placeholder='Nombre' 
                    required 
                    />
                </div>

                <div className='form-group'>
                    <input 
                    type='text' 
                    id='surname' 
                    name='surname' 
                    value={clientData.surname} 
                    onChange={handleChange} 
                    placeholder='Apellido' 
                    required 
                    />
                </div>

                <div className='form-group'>
                    <input 
                    type='text' 
                    id='email' 
                    name='email' 
                    value={clientData.email} 
                    onChange={handleChange} 
                    placeholder='E-mail' 
                    required 
                    />
                </div>

                <div className='form-group'>
                    <input 
                    type='text' 
                    id='cellphone' 
                    name='cellphone'
                    value={clientData.cellphone} 
                    onChange={handleChange} 
                    placeholder='Celular' 
                    required 
                    />
                </div>

                {/* Button and Error message display */}
                <div className='form-actions'>
                    {error && <p className='form-error'>{error}</p>}
                    <button type='submit' className='register-submit-btn'>
                        Registrar Cliente
                    </button>
                </div>
            </form>
        </main>
    );
};

export default ClientsRegister;