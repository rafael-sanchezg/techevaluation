# EJERCICIO PRÁCTICO - INGENIERO DE SOFTWARE

**⏱️ Tiempo:** 150 minutos (2 horas 30 minutos)  
**👤 Modalidad:** Individual

---

## ⚠️ IMPORTANTE

- Al finalizar, **NO enviarás código**
- Solo responderás un **cuestionario de 10 preguntas**
- Las preguntas **solo pueden responderse** si implementaste el ejercicio

---

## 🏦 PROBLEMA: SISTEMA DE NOTIFICACIONES BANCARIAS

### Contexto de Negocio

Tu banco necesita modernizar su sistema de comunicación con clientes. Actualmente, las notificaciones se envían de forma desorganizada y sin control de costos.

### 🎯 Tu Misión

Diseñar e implementar un sistema centralizado de notificaciones que:

1. Soporte múltiples canales de comunicación con diferentes costos operativos
2. Valide destinatarios según reglas específicas de cada canal
3. Clasifique mensajes por nivel de urgencia
4. Mantenga trazabilidad del estado de cada notificación
5. Calcule costos operativos en tiempo real

### 📋 Escenario Real

El departamento de operaciones ha identificado tres canales principales:

- **Email corporativo:** Para comunicaciones formales y estados de cuenta
- **SMS:** Para alertas de seguridad y OTPs (One-Time Passwords)
- **Notificaciones Push:** Para promociones y recordatorios de pago

Cada canal tiene un costo operativo diferente y reglas de validación específicas que debes implementar.

---

## 🛠️ REQUISITOS TÉCNICOS

### 1. Modelo de Datos - Notificación

Debes diseñar una entidad que represente una **Notificación** con los siguientes atributos:

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| **Identificador único** | String (UUID) | Genera automáticamente |
| **Destinatario** | String | Email, número telefónico o device ID |
| **Mensaje** | String | Contenido de la notificación |
| **Canal** | Enum | Tipo de canal (EMAIL, SMS, PUSH) |
| **Prioridad** | Enum | Nivel de urgencia (ALTA, MEDIA, BAJA) |
| **Estado** | Enum | Situación actual (PENDIENTE, ENVIADA, FALLIDA) |
| **Costo** | BigDecimal | Valor monetario del envío |
| **Fecha de creación** | Timestamp | Registro de creación |
| **Fecha de envío** | Timestamp | Cuando se envió (null si está pendiente) |

---

### 2. Patrón Strategy - Canales de Notificación

Implementa el **patrón Strategy** para manejar diferentes canales de envío.

#### 📧 Canal EMAIL
- **Costo fijo:** $0.10 por notificación
- **Validación:** El destinatario debe contener el símbolo "@"
- **Comportamiento:** Simula el envío retornando éxito si la validación pasa

#### 📱 Canal SMS
- **Costo fijo:** $0.50 por notificación
- **Validación:** El destinatario debe tener exactamente 10 dígitos numéricos
- **Comportamiento:** Simula el envío retornando éxito si la validación pasa

#### 🔔 Canal PUSH
- **Costo fijo:** $0.05 por notificación
- **Validación:** El destinatario debe iniciar con el prefijo "device_"
- **Comportamiento:** Simula el envío retornando éxito si la validación pasa

#### Tu Strategy debe poder:
- ✅ Enviar una notificación por el canal correspondiente
- ✅ Calcular el costo del envío
- ✅ Identificar el nombre del canal

---

### 3. Patrón Factory - Creación de Canales

Implementa el patrón **Factory** para crear instancias de las estrategias de canal.

El factory debe:
- Recibir el tipo de canal (EMAIL, SMS, PUSH)
- Retornar la estrategia correspondiente
- Permitir agregar nuevos canales fácilmente (**Open/Closed Principle**)

---

### 4. Servicio de Notificaciones (Aplicar SOLID)

Diseña un servicio que gestione el ciclo de vida de las notificaciones.

#### Operaciones Requeridas:

1. **Crear notificación:** Recibe destinatario, mensaje, canal y prioridad
2. **Enviar notificación:** Procesa el envío usando la estrategia correcta
3. **Obtener por ID:** Busca una notificación específica
4. **Obtener por estado:** Lista notificaciones según su estado
5. **Calcular costo total:** Suma los costos de todas las notificaciones

#### Principios SOLID a Aplicar:

- **SRP:** El servicio solo gestiona lógica de notificaciones
- **OCP:** Debe ser extensible a nuevos canales sin modificar el código base
- **DIP:** Debe depender de abstracciones (Strategy), no de implementaciones concretas

#### Principio DRY (Don't Repeat Yourself):

Crea métodos privados reutilizables para:

1. **Validar destinatario:** No puede ser nulo ni vacío
2. **Validar mensaje:** No puede ser nulo, vacío, ni exceder 500 caracteres
3. **Generar ID:** Usa UUID para crear identificadores únicos

---

### 5. Repositorio en Memoria

Implementa un repositorio simple que almacene notificaciones en memoria usando una estructura `Map`/`HashMap`.

Debe soportar:
- ✅ Guardar una notificación
- ✅ Buscar por ID
- ✅ Listar todas las notificaciones
- ✅ Filtrar por estado
- ✅ Contar registros
- ✅ Limpiar el almacén

---

## 🧪 PRUEBAS REQUERIDAS (MÍNIMO 10 TESTS)

Debes validar tu implementación con pruebas automatizadas usando **JUnit 5** y **Mockito**.

### A) Tests de Servicio con Mocks (Mínimo 7 tests)

1. **Creación exitosa de notificación**
   - Verifica que los datos se asignen correctamente
   - El estado inicial debe ser PENDIENTE
   - Debe calcular el costo según el canal
   - Debe generar un ID único automáticamente

2. **Validación de destinatario nulo o vacío**
   - Debe rechazar destinatarios inválidos
   - No debe persistir datos incorrectos

3. **Validación de longitud de mensaje**
   - Rechaza mensajes que excedan el límite permitido (500 caracteres)
   - Protege la integridad del sistema

4. **Envío exitoso por canal EMAIL**
   - Cambia el estado de PENDIENTE a ENVIADA
   - Registra timestamp de envío
   - Usa la estrategia correcta

5. **Cálculo de costos totales**
   - Suma correctamente los costos de múltiples notificaciones
   - Maneja diferentes canales en el cálculo

6. **Filtrado por estado**
   - Recupera solo notificaciones con estado específico
   - Maneja listas vacías correctamente

7. **Factory genera estrategia correcta**
   - Crea la implementación adecuada según el canal
   - Verifica propiedades de la estrategia creada

### B) Tests de Strategy Pattern (Mínimo 3 tests)

8. **Estrategia de Email**
   - Calcula el costo correcto
   - Valida formato de email

9. **Estrategia de SMS**
   - Calcula el costo correcto
   - Valida formato de número telefónico

10. **Estrategia de Push**
    - Calcula el costo correcto
    - Valida formato de device ID

### Framework de Testing

Usa las siguientes herramientas de Mockito:
- `@Mock`: Para simular dependencias (repositorio, servicios externos)
- `@InjectMocks`: Para inyectar mocks en la clase bajo prueba
- `when().thenReturn()`: Para definir comportamiento de mocks
- `verify()`: Para verificar interacciones con mocks
- `ArgumentCaptor`: Para capturar y validar argumentos pasados a mocks

---

## 📦 ENTREGABLES

### Lo que DEBES implementar:

1. ✅ **Modelo:** Clase `Notificacion` con todos los atributos
2. ✅ **Enums:** `CanalNotificacion`, `Prioridad`, `Estado`
3. ✅ **Strategy Pattern:**
   - Interfaz `CanalNotificacionStrategy`
   - `EmailNotificationStrategy`
   - `SmsNotificationStrategy`
   - `PushNotificationStrategy`
4. ✅ **Factory Pattern:** `NotificacionStrategyFactory`
5. ✅ **Service:**
   - Interfaz `NotificacionService`
   - `NotificacionServiceImpl` con validaciones DRY
6. ✅ **Repository:** `NotificacionRepository` (en memoria)
7. ✅ **Tests:** Mínimo 10 tests (7 unitarios + 3 de strategy)

### Lo que NO necesitas:

- ❌ Base de datos real
- ❌ Tests de integración

---

## ✅ CHECKLIST ANTES DE TERMINAR

### Modelos y Enumeraciones
- [ ] Enum para tipos de canal (EMAIL, SMS, PUSH)
- [ ] Enum para niveles de prioridad (ALTA, MEDIA, BAJA)
- [ ] Enum para estados (PENDIENTE, ENVIADA, FALLIDA)
- [ ] Clase Notificación con 9 atributos mínimos

### Patrones de Diseño
- [ ] Interfaz Strategy con 3 métodos
- [ ] Implementación de estrategia para EMAIL (costo: $0.10)
- [ ] Implementación de estrategia para SMS (costo: $0.50)
- [ ] Implementación de estrategia para PUSH (costo: $0.05)
- [ ] Factory para creación de estrategias

### Capa de Servicio
- [ ] Interfaz de servicio con 5 operaciones
- [ ] Implementación de servicio aplicando SOLID
- [ ] 3 métodos privados para validaciones DRY

### Persistencia
- [ ] Repositorio en memoria funcional
- [ ] Métodos de búsqueda, guardado y filtrado

### Pruebas
- [ ] 7+ tests unitarios del servicio con Mockito
- [ ] 3+ tests de estrategias
- [ ] Todos los tests ejecutan exitosamente (verde)

---

## 📊 INFORMACIÓN CLAVE DE IMPLEMENTACIÓN

> ⚠️ **Anota estos detalles durante tu desarrollo** (serán necesarios para el cuestionario)

### 💰 Costos por Canal
- **Email:** $0.10 por notificación
- **SMS:** $0.50 por notificación
- **Push:** $0.05 por notificación

### 🔍 Reglas de Validación
- **Email:** Destinatario debe contener "@"
- **SMS:** Destinatario debe tener exactamente 10 dígitos
- **Push:** Destinatario debe iniciar con "device_"
- **Mensaje:** Máximo 500 caracteres

### ⚙️ Especificaciones Técnicas
- **Estado inicial:** PENDIENTE
- **Tipo de ID:** UUID (String)
- **Cantidad de métodos en Strategy:** 3
- **Cantidad de validaciones privadas (DRY):** 3
- **Implementaciones de Strategy:** 3 (una por canal)

### 🧪 Testing
- **Tests mínimos requeridos:** 10
- **Anotación para mocks:** @Mock
- **Framework:** JUnit 5 + Mockito

---

## 🚀 ¡Buena suerte!

**Tiempo estimado:** 60-150 minutos

---

*Ejercicio diseñado para evaluar conocimientos en patrones de diseño, principios SOLID, y testing en Java.*

