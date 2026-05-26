-- Providers
MERGE INTO providers (id, name, tax_identification_number, email, status) KEY (id) VALUES
('a0000000-0000-0000-0000-000000000001', 'TecnoSoluciones S.A.', 'J-12345678-5', 'contacto@tecnosoluciones.com', 'ACTIVE'),
('a0000000-0000-0000-0000-000000000002', 'Servicios Generales C.A.', 'J-23456789-6', 'info@serviciosgenerales.com', 'ACTIVE'),
('a0000000-0000-0000-0000-000000000003', 'Inversiones del Sur S.A.S.', 'J-34567890-7', 'admin@inversionesdelsur.com', 'ACTIVE');

-- Payment Orders
MERGE INTO payment_orders (id, provider_id, provider_name, amount, description, creation_date, update_date, status, idempotency_key) KEY (id) VALUES
('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'TecnoSoluciones S.A.', 1500.00, 'Pago de servicios cloud - mensualidad marzo', '2025-03-01T10:00:00', '2025-03-01T10:05:00', 'PAID', 'idem-key-001'),
('b0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', 'TecnoSoluciones S.A.', 250.75, 'Licencias de software - renovación Q1', '2025-03-15T14:30:00', NULL, 'DRAFT', 'idem-key-002'),
('b0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000002', 'Servicios Generales C.A.', 3200.00, 'Mantenimiento de equipos - contrato anual', '2025-04-01T08:15:00', '2025-04-02T09:00:00', 'PAID', 'idem-key-003'),
('b0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000002', 'Servicios Generales C.A.', 875.50, 'Limpieza de oficinas - servicio mensual', '2025-04-10T11:00:00', NULL, 'APPROVED', 'idem-key-004'),
('b0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000003', 'Inversiones del Sur S.A.S.', 5400.00, 'Consultoría financiera - proyecto Q2', '2025-05-05T09:00:00', '2025-05-06T10:30:00', 'PAID', 'idem-key-005'),
('b0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000003', 'Inversiones del Sur S.A.S.', 1200.00, 'Auditoría externa - informe trimestral', '2025-05-20T15:45:00', NULL, 'APPROVED', 'idem-key-006'),
('b0000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000001', 'TecnoSoluciones S.A.', 9800.00, 'Infraestructura de servidores - migración', '2025-06-01T07:00:00', '2025-06-03T12:00:00', 'REJECTED', 'idem-key-007'),
('b0000000-0000-0000-0000-000000000008', 'a0000000-0000-0000-0000-000000000002', 'Servicios Generales C.A.', 450.00, 'Suministros de oficina - pedido mayo', '2025-06-10T10:30:00', NULL, 'DRAFT', 'idem-key-008'),
('b0000000-0000-0000-0000-000000000009', 'a0000000-0000-0000-0000-000000000001', 'TecnoSoluciones S.A.', 3200.00, 'Soporte técnico premium - mensual junio', '2025-06-15T13:00:00', '2025-06-16T08:00:00', 'APPROVED', 'idem-key-009'),
('b0000000-0000-0000-0000-000000000010', 'a0000000-0000-0000-0000-000000000003', 'Inversiones del Sur S.A.S.', 6750.00, 'Desarrollo de software - módulo contable', '2025-07-01T09:00:00', NULL, 'APPROVED', 'idem-key-010');
