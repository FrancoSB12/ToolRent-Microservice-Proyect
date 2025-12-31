import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import employeeService from '../../services/employeeService.js';
import '../../styles/Register.css';

const ROLE_OPTIONS = [
    { value: 'false', label: 'Empleado'},       //false = isAdmin false
    { value: 'true', label: 'Administrador'}    //true = isAdmin true
]

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

const EmployeeRegister = () => {
    const navigate = useNavigate();

    const [employeeData, setEmployeeData] = useState({
        run: '',
        name: '',
        surname: '',
        email: '',
        cellphone: '',
        isAdmin: ''
    });

    const [password, setPassword] = useState('');

    const [error, setError] = useState(null);
    
    /* Handle input changes */
    const handleChange = (e) => {
        const { name, value } = e.target;

        if(name === 'run'){
            setEmployeeData(prevState => ({
                ...prevState,
                [name]: formatRun(value)
            }));
            return;

        } else if(name === 'password'){
            setPassword(value);

        } else{
            setEmployeeData(prevState => ({
                    ...prevState,
                    [name]: value
                }));
        }
    }

    const handleSubmit = (e) => {
        e.preventDefault();
        setError(null);

        const dataToSend = {
            ...employeeData,
            isAdmin: employeeData.isAdmin === 'true'    //If the string is 'true', then send the boolean true
        }

        employeeService.create(dataToSend, password)
        .then(response => {
            toast.success('Empleado registrado con éxito');
            navigate('/employees');
        })
        .catch(err => {
            console.error('Error al registrar:', err);
            if(err.response && err.response.data){
                {/* err.response exists if the backend server responded */}
                if(typeof err.response.data === 'string'){
                    setError(err.response.data);
                } else {
                    setError(err.response.data.message || 'Ocurrió un error al registrar al empleado.');
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
            <h2 className='form-title'>Registrar Nuevo Empleado</h2>
            <form className='register-form' onSubmit={handleSubmit}>
                {/* Text fields */}
                <div className='form-group'>
                    <input 
                        type='text' 
                        id='run' 
                        name='run' 
                        value={employeeData.run} 
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
                        value={employeeData.name} 
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
                        value={employeeData.surname} 
                        onChange={handleChange} 
                        placeholder='Apellido' 
                        required 
                    />
                </div>

                <div className="form-group">
                    <input 
                        type="password" 
                        name="password" 
                        value={password} 
                        onChange={handleChange} 
                        placeholder="Contraseña" 
                        required 
                        minLength={4} 
                    />
                </div>

                <div className='form-group'>
                    <input 
                        type='email' 
                        id='email' 
                        name='email' 
                        value={employeeData.email} 
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
                        value={employeeData.cellphone} 
                        onChange={handleChange} 
                        placeholder='Celular' 
                        required 
                    />
                </div>

                {/* Selection field */}
                <div className="form-group">
                    <label htmlFor="isAdmin">Rol en el Sistema</label>
                    <select 
                        id="isAdmin" 
                        name="isAdmin" 
                        value={employeeData.isAdmin} 
                        onChange={handleChange}
                        style={{ color: employeeData.isAdmin === '' ? '#888' : 'inherit'}}
                    >
                        <option value="" disabled>Seleccione un Rol</option>
                        {ROLE_OPTIONS.map(option => (
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
                        Registrar Empleado
                    </button>
                </div>
            </form>
        </main>
    );
};

export default EmployeeRegister;