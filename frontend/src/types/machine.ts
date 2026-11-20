export type MachineConnectionStatus = 'ONLINE' | 'OFFLINE'

export type MachineOperationalStatus =
  | 'ACTIVE'
  | 'MAINTENANCE'
  | 'DISABLED'
  | 'ERROR'

export interface Machine {
  id: number
  tenantId: number
  tenantName: string
  locationId: number | null
  locationName: string
  machineCode: string
  serialNumber: string
  deviceIdentity: string
  connectionStatus: MachineConnectionStatus
  operationalStatus: MachineOperationalStatus
  firmwareVersion: string
  lastPingAt: string | null
  createdAt: string
  updatedAt?: string
}

export interface MachineRequest {
  tenantId: number
  locationId: number | null
  machineCode: string
  serialNumber: string
  deviceIdentity: string
  connectionStatus: MachineConnectionStatus
  operationalStatus: MachineOperationalStatus
  firmwareVersion: string
}
