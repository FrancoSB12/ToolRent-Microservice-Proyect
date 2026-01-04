import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import toolItemService from '../../services/toolItemService.js';
import '../../styles/Register.css';

//Damage evaluation options
const DAMAGE_OPTIONS = [
  { value: 'NO_DANADA', label: 'Sin Daños (Falsa Alarma)' },
  { value: 'LEVEMENTE_DANADA', label: 'Daño Leve (Reparable)' },
  { value: 'DANADA', label: 'Dañada (Reparable con Costo)' },
  { value: 'GRAVEMENTE_DANADA', label: 'Gravemente Dañada (Reparación Mayor)' },
  { value: 'IRREPARABLE', label: 'Irreparable / Pérdida Total' }
];

const ToolItemEvaluateDamage = () => {
  const navigate = useNavigate();

  const [serialNumber, setSerialNumber] = useState('');
  const [tool, setTool] = useState(null);
  const [damageLevel, setDamageLevel] = useState(''); 
  const [loading, setLoading] = useState(false);

  //Search the tool item by serial number
  const handleSearch = async (e) => {
    e.preventDefault();
    if (!serialNumber) return;
    
    setLoading(true);
    try {
      const response = await toolItemService.getItemBySerialNumber(serialNumber);
      const foundTool = response.data;
      
      if (foundTool.status === 'DISPONIBLE') {
        toast.info("Esta herramienta figura como DISPONIBLE. ¿Seguro que es la correcta?");
      }
      
      setTool(foundTool);
      setDamageLevel(''); 

    } catch (err) {
      console.error(err);
      toast.error("No se encontró ninguna herramienta con ese número de serie.");
      setTool(null);
      
    } finally {
      setLoading(false);
    }
  };

  //Submit the evaluation
  const handleSubmitEvaluation = async () => {
    if (!tool || !damageLevel) {
      toast.warning("Debe seleccionar un resultado de la evaluación.");
      return;
    }

    try {
      const evaluationData = {
        id: tool.id,
        damageLevel: damageLevel
        // The backend is responsible for finding the loan and the client
      };

      await toolItemService.evaluateItemDamage(tool.id, evaluationData);

      toast.success(`Evaluación registrada. Se han aplicado los cargos correspondientes.`);
      navigate('/tools');

    } catch (err) {
      console.error(err);
      if (err.response && err.response.data) {
        toast.error(`Error: ${err.response.data}`);
      } else {
        toast.error("Error al procesar la evaluación.");
      }
    }
  };

  return (
    <main className="full-page-content">
      <h2 className="form-title">Evaluar Daño y Cobros</h2>

      {/* --- Search bar --- */}
      <div style={{ maxWidth: '600px', margin: '0 auto 2rem auto' }}>
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '1rem' }}>
          <div className="form-group" style={{ flexGrow: 1, marginBottom: 0 }}>
            <input 
              type="text" 
              value={serialNumber} 
              onChange={(e) => setSerialNumber(e.target.value)} 
              placeholder="Escanear Número de Serie *" 
              required 
            />
          </div>
          <button type="submit" className="register-submit-btn" style={{ width: 'auto', padding: '0 2rem' }} disabled={loading}>
            {loading ? 'Buscando...' : 'Buscar'}
          </button>
        </form>
      </div>

      {/* --- Evaluation form (Only if the tool was found) --- */}
      {tool && (
        <div className="register-form" style={{ maxWidth: '700px', margin: '0 auto', display: 'block' }}>
            
          {/* Tool data */}
          <div style={{ background: '#f4f4f4', padding: '1.5rem', borderRadius: '8px', marginBottom: '2rem' }}>
            <h3 style={{ marginTop: 0, color: '#333' }}>{tool.toolType?.name}</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', fontSize: '0.95rem' }}>
              <div><strong>Modelo:</strong> {tool.toolType?.model}</div>
              <div><strong>Serie:</strong> {tool.serialNumber}</div>
              <div><strong>Estado Actual:</strong> {tool.status}</div>
              <div><strong>Daño Actual:</strong> {tool.damageLevel}</div>
              <div><strong>Valor Reposición:</strong> ${tool.toolType?.replacementValue}</div>
              <div><strong>Costo Reparación:</strong> ${tool.toolType?.damageFee}</div>
            </div>
          </div>

          {/* New Status Selector */}
          <div className="form-group">
            <label style={{ fontWeight: 'bold', marginBottom: '0.5rem', display: 'block' }}>Resultado de la Evaluación:</label>
                
            <select 
              value={damageLevel} 
              onChange={(e) => setDamageLevel(e.target.value)}
              required
              style={{ 
                height: '56px', 
                width: '100%', 
                padding: '0 1rem', 
                borderRadius: '8px', 
                border: '1px solid #ccc',
                color: damageLevel === '' ? '#888' : (damageLevel === 'IRREPARABLE' ? 'red' : (damageLevel !== 'NO_DANADA' ? '#d9534f' : '#333')),
                fontWeight: damageLevel && damageLevel !== 'NO_DANADA' ? 'bold' : 'normal'
              }}
            >
              <option value="" disabled hidden>⬇ Seleccione el resultado de la evaluación *</option>
                      
              {DAMAGE_OPTIONS.map(opt => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </div>

          {/* Informative message about the action */}
          {damageLevel !== '' && (
            <div style={{ margin: '1.5rem 0', padding: '1rem', borderLeft: '4px solid #007bff', background: '#e7f1ff', fontSize: '0.9rem' }}>
              <strong>Acción del Sistema:</strong>
              {damageLevel === 'IRREPARABLE' ? (
                <ul style={{ margin: '0.5rem 0 0 1.5rem' }}>
                  <li>La herramienta se dará de <strong>BAJA</strong>.</li>
                  <li>Se cobrará el <strong>Valor de Reposición (${tool.toolType?.replacementValue})</strong> al último cliente.</li>
                </ul>
              ) : damageLevel === 'NO_DANADA' ? (
                <ul style={{ margin: '0.5rem 0 0 1.5rem' }}>
                  <li>La herramienta volverá a estar <strong>DISPONIBLE</strong>.</li>
                  <li>No se aplicarán cargos extra.</li>
                  </ul>
              ) : (
                <ul style={{ margin: '0.5rem 0 0 1.5rem' }}>
                  <li>La herramienta quedará <strong>EN REPARACIÓN</strong>.</li>
                  <li>Se cobrará la <strong>Tarifa de Daño (${tool.toolType?.damageFee})</strong> al último cliente.</li>
                </ul>
              )}
            </div>
          )}

          {/* Buttons */}
          <div className="form-actions">
            <button 
              type="button" 
              onClick={handleSubmitEvaluation}
              className="register-submit-btn"
              style={{ backgroundColor: '#d9534f' }} // Rojo para indicar cobro
            >
              Confirmar Evaluación y Cobro
            </button>
                
            <button 
              type="button" 
              onClick={() => { setTool(null); setSerialNumber(''); setDamageLevel(''); }}
              style={{ background: 'none', border: 'none', marginTop: '1rem', cursor: 'pointer', textDecoration: 'underline', color: '#666' }}
            >
              Cancelar / Nueva Búsqueda
            </button>
          </div>
        </div>
      )}
    </main>
  );
};

export default ToolItemEvaluateDamage;