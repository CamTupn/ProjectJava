# Báo cáo kiểm thử hệ thống ScentStation

Ngày ghi nhận: 26/11/2026. Nguồn: Surefire XML trong bản source review; không chạy lại Maven tại môi trường soạn tài liệu.

## 1. Kết quả regression

| Test class | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| AuthorizationTestControllerTest | 6 | 0 | 0 | 0 |
| HealthControllerTest | 1 | 0 | 0 | 0 |
| MachineControllerSecurityTest | 48 | 0 | 0 | 0 |
| MachineControllerTest | 2 | 0 | 0 | 0 |
| TenantControllerTest | 4 | 0 | 0 | 0 |
| UserControllerSecurityTest | 11 | 0 | 0 | 0 |
| ScentstationBackendApplicationTests | 1 | 0 | 0 | 0 |
| AuthServiceTest | 7 | 0 | 0 | 0 |
| JwtServiceTest | 6 | 0 | 0 | 0 |
| MachineServiceTest | 39 | 0 | 0 | 0 |
| UserServiceTest | 10 | 0 | 0 | 0 |
| **Tổng** | 135 | 0 | 0 | 0 |

## 2. Chi tiết từng test tự động

### AuthorizationTestControllerTest

| Test case | Kết quả | Thời gian (s) |
|---|---|---:|
| `brandAdminShouldNotAccessTechnicianEndpoint` | PASS | 0.208 |
| `brandAdminShouldNotAccessPlatformAdminEndpoint` | PASS | 0.011 |
| `technicianShouldNotAccessBrandAdminEndpoint` | PASS | 0.008 |
| `technicianShouldAccessTechnicianEndpoint` | PASS | 0.011 |
| `brandAdminShouldAccessBrandAdminEndpoint` | PASS | 0.007 |
| `platformAdminShouldAccessPlatformAdminEndpoint` | PASS | 0.006 |

### HealthControllerTest

| Test case | Kết quả | Thời gian (s) |
|---|---|---:|
| `healthCheck_ShouldReturnUpStatus` | PASS | 0.047 |

### MachineControllerSecurityTest

| Test case | Kết quả | Thời gian (s) |
|---|---|---:|
| `inaccessibleMachineConnectionUpdateShouldReturn404` | PASS | 0.075 |
| `nonAdminRolesShouldNotUpdateMachine(String)[1]` | PASS | 0.113 |
| `nonAdminRolesShouldNotUpdateMachine(String)[2]` | PASS | 0.008 |
| `nonAdminRolesShouldNotUpdateMachine(String)[3]` | PASS | 0.008 |
| `unauthenticatedUserShouldNotCreateMachine` | PASS | 0.006 |
| `inventoryStaffShouldNotAccessMachineDetail` | PASS | 0.006 |
| `inventoryStaffShouldNotUpdateOperationalStatus` | PASS | 0.006 |
| `unauthenticatedUserShouldNotUpdateConnectionStatus` | PASS | 0.004 |
| `nonAdminShouldNotSetDisabledStatus(String)[1]` | PASS | 0.007 |
| `nonAdminShouldNotSetDisabledStatus(String)[2]` | PASS | 0.008 |
| `unauthorizedRolesShouldNotUpdateConnectionStatus(String)[1]` | PASS | 0.007 |
| `unauthorizedRolesShouldNotUpdateConnectionStatus(String)[2]` | PASS | 0.005 |
| `unauthorizedRolesShouldNotUpdateConnectionStatus(String)[3]` | PASS | 0.006 |
| `unauthenticatedUserShouldNotUpdateMachine` | PASS | 0.004 |
| `manualOnlineStatusShouldReturn403(String)[1]` | PASS | 0.006 |
| `manualOnlineStatusShouldReturn403(String)[2]` | PASS | 0.007 |
| `nonAdminRolesShouldNotDeleteMachine(String)[1]` | PASS | 0.006 |
| `nonAdminRolesShouldNotDeleteMachine(String)[2]` | PASS | 0.006 |
| `nonAdminRolesShouldNotDeleteMachine(String)[3]` | PASS | 0.005 |
| `unauthenticatedUserShouldNotUpdateOperationalStatus` | PASS | 0.006 |
| `adminRolesShouldCreateMachine(String)[1]` | PASS | 0.023 |
| `adminRolesShouldCreateMachine(String)[2]` | PASS | 0.007 |
| `unauthenticatedUserShouldNotDeleteMachine` | PASS | 0.003 |
| `adminRolesShouldUpdateMachine(String)[1]` | PASS | 0.007 |
| `adminRolesShouldUpdateMachine(String)[2]` | PASS | 0.008 |
| `authorizedRolesShouldUpdateConnectionStatus(String)[1]` | PASS | 0.005 |
| `authorizedRolesShouldUpdateConnectionStatus(String)[2]` | PASS | 0.005 |
| `unauthenticatedUserShouldNotAccessMachineList` | PASS | 0.004 |
| `inventoryStaffShouldNotAccessMachineList` | PASS | 0.008 |
| `adminRolesShouldDisableMachine(String)[1]` | PASS | 0.006 |
| `adminRolesShouldDisableMachine(String)[2]` | PASS | 0.005 |
| `inaccessibleMachineShouldReturn404` | PASS | 0.006 |
| `authorizedRolesShouldAccessMachineList(String)[1]` | PASS | 0.016 |
| `authorizedRolesShouldAccessMachineList(String)[2]` | PASS | 0.004 |
| `authorizedRolesShouldAccessMachineList(String)[3]` | PASS | 0.006 |
| `authorizedRolesShouldAccessMachineList(String)[4]` | PASS | 0.005 |
| `authorizedRolesShouldUpdateOperationalStatus(String)[1]` | PASS | 0.004 |
| `authorizedRolesShouldUpdateOperationalStatus(String)[2]` | PASS | 0.005 |
| `authorizedRolesShouldUpdateOperationalStatus(String)[3]` | PASS | 0.006 |
| `authorizedRolesShouldUpdateOperationalStatus(String)[4]` | PASS | 0.005 |
| `nonAdminRolesShouldNotCreateMachine(String)[1]` | PASS | 0.005 |
| `nonAdminRolesShouldNotCreateMachine(String)[2]` | PASS | 0.006 |
| `nonAdminRolesShouldNotCreateMachine(String)[3]` | PASS | 0.005 |
| `unauthenticatedUserShouldNotAccessMachineDetail` | PASS | 0.003 |
| `authorizedRolesShouldAccessMachineDetail(String)[1]` | PASS | 0.005 |
| `authorizedRolesShouldAccessMachineDetail(String)[2]` | PASS | 0.006 |
| `authorizedRolesShouldAccessMachineDetail(String)[3]` | PASS | 0.006 |
| `authorizedRolesShouldAccessMachineDetail(String)[4]` | PASS | 0.004 |

### MachineControllerTest

| Test case | Kết quả | Thời gian (s) |
|---|---|---:|
| `getMachineById_WhenNotFound_ShouldReturn404` | PASS | 0.01 |
| `getAllMachines_ShouldReturnMachineList` | PASS | 0.011 |

### TenantControllerTest

| Test case | Kết quả | Thời gian (s) |
|---|---|---:|
| `getTenantById_WhenIdIsInvalid_ShouldReturn400` | PASS | 0.015 |
| `getAllTenants_ShouldReturnTenantList` | PASS | 0.009 |
| `getTenantById_ShouldReturnTenant` | PASS | 0.006 |
| `getTenantById_WhenNotFound_ShouldReturn404` | PASS | 0.008 |

### UserControllerSecurityTest

| Test case | Kết quả | Thời gian (s) |
|---|---|---:|
| `operationsManagerShouldNotAccessUserList` | PASS | 0.013 |
| `technicianShouldNotUpdateUser` | PASS | 0.024 |
| `brandAdminShouldAccessUpdateStatusEndpoint` | PASS | 0.008 |
| `superAdminShouldAccessUserList` | PASS | 0.005 |
| `inventoryStaffShouldNotLockUser` | PASS | 0.006 |
| `unauthenticatedUserShouldNotAccessUserList` | PASS | 0.003 |
| `brandAdminShouldAccessUserList` | PASS | 0.005 |
| `technicianShouldNotAccessUserList` | PASS | 0.004 |
| `operationsManagerShouldNotLockUser` | PASS | 0.004 |
| `operationsManagerShouldNotAccessUserDetail` | PASS | 0.005 |
| `inventoryStaffShouldNotAccessUserList` | PASS | 0.004 |

### ScentstationBackendApplicationTests

| Test case | Kết quả | Thời gian (s) |
|---|---|---:|
| `contextLoads` | PASS | 0.009 |

### AuthServiceTest

| Test case | Kết quả | Thời gian (s) |
|---|---|---:|
| `loginWithLockedAccountShouldThrowException` | PASS | 0.066 |
| `loginWithUnknownAccountShouldThrowException` | PASS | 0.003 |
| `loginWithEmailShouldReturnAuthResponse` | PASS | 0.002 |
| `loginWithUsernameShouldReturnAuthResponse` | PASS | 0.002 |
| `loginWithInactiveAccountShouldThrowException` | PASS | 0.004 |
| `loginWithWrongPasswordShouldThrowException` | PASS | 0.003 |
| `loginWithoutTenantShouldReturnNullTenantCode` | PASS | 0.003 |

### JwtServiceTest

| Test case | Kết quả | Thời gian (s) |
|---|---|---:|
| `shouldReturnFalseForWrongUsername` | PASS | 0.087 |
| `shouldReturnFalseForTamperedToken` | PASS | 0.001 |
| `shouldDetectExpiredToken` | PASS | 2.504 |
| `shouldGenerateTokenSuccessfully` | PASS | 0.002 |
| `shouldReturnTrueForValidToken` | PASS | 0.001 |
| `shouldExtractCustomClaims` | PASS | 0.001 |

### MachineServiceTest

| Test case | Kết quả | Thời gian (s) |
|---|---|---:|
| `updateMachine_DuplicateSerialNumber_Throws409` | PASS | 0.182 |
| `getAll_InvalidConnectionStatus_ThrowsException` | PASS | 0.002 |
| `getAll_FilterByOperationalStatus` | PASS | 0.003 |
| `createMachine_WithDuplicateSerialNumber_Throws409` | PASS | 0.003 |
| `createMachine_WithInactiveLocation_Throws409` | PASS | 0.003 |
| `updateOperationalStatus_ByBrandAdmin_ReenableDisabled_Success` | PASS | 0.001 |
| `getAll_RequestOtherTenant_Throws404` | PASS | 0.001 |
| `updateMachine_DuplicateMachineCode_Throws409` | PASS | 0.003 |
| `getAll_ByBrandAdmin_OnlySearchOwnTenant` | PASS | 0.003 |
| `updateConnectionStatus_ByPlatformAdmin_ToOnline_Throws403` | PASS | 0.002 |
| `updateMachine_DuplicateDeviceIdentity_Throws409` | PASS | 0.002 |
| `updateConnectionStatus_ByPlatformAdmin_ToOffline_Success` | PASS | 0.003 |
| `deleteMachine_OtherTenantMachine_Throws404` | PASS | 0.003 |
| `getById_OtherTenantMachine_Throws404` | PASS | 0.003 |
| `createMachine_WithOtherTenantLocation_Throws404` | PASS | 0.002 |
| `softDelete_SetsOperationalStatusDisabled` | PASS | 0.004 |
| `updateOperationalStatus_OtherTenantMachine_Throws404` | PASS | 0.002 |
| `updateConnectionStatus_ByInventoryStaff_Throws403` | PASS | 0.001 |
| `getAll_FilterByLocationId` | PASS | 0.002 |
| `updateMachine_ValidRequest_Success` | PASS | 0.003 |
| `updateOperationalStatus_InvalidStatus_ThrowsException` | PASS | 0.002 |
| `updateConnectionStatus_ByBrandAdmin_Throws403` | PASS | 0.002 |
| `updateConnectionStatus_ByTechnician_UpdatesConnectionOnly` | PASS | 0.002 |
| `getAll_ByPlatformAdmin_SearchAllTenants` | PASS | 0.002 |
| `updateMachine_WithOtherTenantLocation_Throws404` | PASS | 0.003 |
| `updateConnectionStatus_OtherTenantMachine_Throws404` | PASS | 0.002 |
| `createMachine_ByBrandAdmin_ForOtherTenant_Throws403` | PASS | 0.002 |
| `updateConnectionStatus_ByOperationsManager_Throws403` | PASS | 0.002 |
| `updateConnectionStatus_ByTechnician_ToOnline_Throws403` | PASS | 0.002 |
| `updateOperationalStatus_DoesNotModifyConnectionStatus` | PASS | 0.002 |
| `getAll_FilterByConnectionStatus` | PASS | 0.002 |
| `createMachine_ByBrandAdmin_DefaultsOfflineAndActive` | PASS | 0.003 |
| `createMachine_WithDuplicateMachineCode_Throws409` | PASS | 0.004 |
| `createMachine_WithDuplicateDeviceIdentity_Throws409` | PASS | 0.002 |
| `updateOperationalStatus_ByTechnician_ReenableDisabled_Throws403` | PASS | 0.002 |
| `inventoryStaff_CannotAccessMachines` | PASS | 0.002 |
| `updateOperationalStatus_ByTechnician_ToDisabled_Throws403` | PASS | 0.002 |
| `updateConnectionStatus_InvalidStatus_ThrowsException` | PASS | 0.002 |
| `updateMachine_OtherTenantMachine_Throws404` | PASS | 0.001 |

### UserServiceTest

| Test case | Kết quả | Thời gian (s) |
|---|---|---:|
| `brandAdminShouldNotLockAnotherBrandAdmin` | PASS | 0.038 |
| `superAdminShouldUpdateAnyUser` | PASS | 0.003 |
| `brandAdminShouldUpdateEmployeeInSameTenant` | PASS | 0.002 |
| `brandAdminShouldNotUpdateAnotherBrandAdmin` | PASS | 0.002 |
| `brandAdminShouldNotUpdateOwnAdminAccount` | PASS | 0.002 |
| `invalidUserStatusShouldBeRejected` | PASS | 0.002 |
| `superAdminShouldNotLockOwnAccount` | PASS | 0.002 |
| `brandAdminShouldLockEmployeeInSameTenant` | PASS | 0.002 |
| `brandAdminShouldNotUpdateOtherTenantUser` | PASS | 0.002 |
| `technicianShouldNotUpdateUsers` | PASS | 0.002 |

## 3. Bằng chứng và giới hạn



Ảnh Postman/MySQL trong cuộc trao đổi xác nhận: GET filter OFFLINE trả 200 và có ID 8; PATCH OFFLINE được phép trả 200; invalid status trả 400; request ngoài tenant tới ID 7 trả 404; request không đủ quyền trả 403. DB cho thấy ID 8 tenant 1/location 4 vẫn OFFLINE/ACTIVE; ID 7 tenant 2 vẫn OFFLINE/ERROR. Không suy ra role từ token bị che hoặc khẳng định đã đổi trạng thái thực tế khi giá trị trước/sau giống nhau.

### Giới hạn

89/89 Machine tests và 135/135 regression tests PASS. Controller tests dùng mock service; security tests dùng mock authentication; service tests dùng mock repository. Không tương đương kiểm thử JWT thật, MySQL transaction/concurrency, MQTT heartbeat hoặc code coverage 100%. Không có kết quả chạy lại toàn bộ collection. Dữ liệu seed/ID và password phải cấu hình phù hợp nếu chạy lại. Merge chưa xác nhận.


Location/Product chưa có test class riêng trong báo cáo XML này; không suy ra PASS từ kết quả regression. Các module kiosk, payment, dispensing, MQTT, inventory và maintenance chưa có bằng chứng kiểm thử trong bộ XML.
