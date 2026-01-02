import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { 
    Table, TableBody, TableCell, TableContainer, 
    TableHead, TableRow, Paper, Chip, Button
} from '@mui/material';
import WarningIcon from '@mui/icons-material/Warning';
import EmailIcon from '@mui/icons-material/Email';
import PhoneIcon from '@mui/icons-material/Phone';
import reportService from '../../services/reportService'; 
import '../../styles/ViewsHome.css'; 

const OverdueClientsView = () => {
    const [overdueList, setOverdueList] = useState([]);
    const [loading, setLoading] = useState(true);

    const fetchOverdueReport = () => {
        setLoading(true);
        reportService.getOverdueClients()
            .then(res => {
                setOverdueList(res.data || []);
                setLoading(false);
            })
            .catch(err => {
                console.error("Error cargando reporte:", err);
                toast.error("Error al obtener el reporte de atrasos.");
                setLoading(false);
            });
    };

    useEffect(() => {
        fetchOverdueReport();
    }, []);

    const formatDate = (dateString) => {
        if (!dateString) return "-";
        return new Date(dateString).toLocaleDateString();
    };

    const getUrgencyColor = (days) => {
        if (days > 30) return "error";
        if (days > 7) return "warning";
        return "info";
    };

    if (loading) {
        return <main className="full-page-content"><p>Cargando reporte de atrasados...</p></main>;
    }

    return (
        <main className="full-page-content">
            
            <div style={{ margin: '0 auto', maxWidth: '1200px' }}>
                
                {/* Warning title */}
                <h2 className="form-title" style={{ 
                    backgroundColor: '#d32f2f',
                    color: 'white',
                    padding: '1rem',
                    borderRadius: '8px',
                    display: 'flex', 
                    alignItems: 'center', 
                    justifyContent: 'center', 
                    gap: '15px',
                    boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
                }}>
                    <WarningIcon fontSize="large" style={{ color: 'white' }} /> 
                    Clientes con Arriendos Atrasados
                </h2>

                {/* Update button */}
                <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '1rem', marginTop: '1rem' }}>
                    <Button 
                        variant="contained" 
                        onClick={fetchOverdueReport}
                        style={{
                            backgroundColor: '#1976d2',
                            color: 'white',
                            fontWeight: 'bold'
                        }}
                    >
                        Actualizar Lista
                    </Button>
                </div>

                {/* Table of overdue clients */}
                <TableContainer component={Paper} elevation={3} style={{ maxHeight: 700 }}>
                    <Table stickyHeader aria-label="overdue table">
                        <TableHead>
                            <TableRow>
                                <TableCell style={{ backgroundColor: '#ffebee', fontWeight: 'bold' }}>ID Arriendo</TableCell>
                                <TableCell style={{ backgroundColor: '#ffebee', fontWeight: 'bold' }}>Cliente (RUT)</TableCell>
                                <TableCell style={{ backgroundColor: '#ffebee', fontWeight: 'bold' }}>Nombre</TableCell>
                                <TableCell style={{ backgroundColor: '#ffebee', fontWeight: 'bold' }}>Contacto</TableCell>
                                <TableCell style={{ backgroundColor: '#ffebee', fontWeight: 'bold' }}>Fecha que Debió Devolver</TableCell>
                                <TableCell align="center" style={{ backgroundColor: '#ffebee', fontWeight: 'bold' }}>Días Atraso</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {overdueList.length > 0 ? (
                                overdueList.map((item, index) => (
                                    <TableRow key={`${item.rentId}-${index}`} hover>
                                        <TableCell>
                                            <strong>#{item.rentId}</strong>
                                            <div style={{ fontSize: '0.8rem', color: '#666' }}>
                                                Desde: {formatDate(item.rentDate)}
                                            </div>
                                        </TableCell>
                                        
                                        <TableCell>{item.run}</TableCell>
                                        
                                        <TableCell style={{ textTransform: 'capitalize' }}>
                                            {item.name.toLowerCase()}
                                        </TableCell>
                                        
                                        <TableCell>
                                            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', fontSize: '0.85rem' }}>
                                                {item.email && (
                                                    <span style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                                                        <EmailIcon fontSize="inherit" color="action" /> {item.email}
                                                    </span>
                                                )}
                                                {item.cellphone && (
                                                    <span style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                                                        <PhoneIcon fontSize="inherit" color="action" /> {item.cellphone}
                                                    </span>
                                                )}
                                            </div>
                                        </TableCell>
                                        
                                        <TableCell style={{ color: '#d32f2f', fontWeight: '500' }}>
                                            {formatDate(item.expectedReturnDate)}
                                        </TableCell>
                                        
                                        <TableCell align="center">
                                            <Chip 
                                                label={`${item.daysOverdue} días`} 
                                                color={getUrgencyColor(item.daysOverdue)}
                                                variant={item.daysOverdue > 30 ? "filled" : "outlined"}
                                                style={{ fontWeight: 'bold' }}
                                            />
                                        </TableCell>
                                    </TableRow>
                                ))
                            ) : (
                                <TableRow>
                                    <TableCell colSpan={6} align="center" style={{ padding: '3rem' }}>
                                        <div style={{ color: '#2e7d32', fontSize: '1.2rem' }}>
                                            ¡Excelente! No hay clientes con arriendos atrasados en este momento.
                                        </div>
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
            </div>
        </main>
    );
};

export default OverdueClientsView;