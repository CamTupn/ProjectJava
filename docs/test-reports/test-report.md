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

## Kiểm thử quản lý khe và regression sau tích hợp

Ngày 27/11/2026 00:18:48 +07:00, Windows PowerShell, `./mvnw.cmd clean test` (trên Windows: `.\mvnw.cmd clean test`). Nhánh fix/task-020-machine-slot-management. Kết quả từ ảnh terminal do nhóm cung cấp: BUILD SUCCESS, 45.547 s, 229 tests, 0 failures, 0 errors, 0 skipped. PR #64 gốc đã merge; bản sửa chưa merge tại thời điểm báo cáo.

| Bộ test | Số lượt | Failure | Error | Skipped |
|---|---:|---:|---:|---:|
| MachineSlotServiceTest | 42 | 0 | 0 | 0 |
| MachineSlotControllerTest | 4 | 0 | 0 | 0 |
| MachineSlotControllerSecurityTest | 48 | 0 | 0 | 0 |
| Các module trước | 135 | 0 | 0 | 0 |
| Tổng | 229 | 0 | 0 | 0 |

Phân rã dựa trên source và tổng kết Maven; không có Surefire XML trong ZIP.

### Danh mục Service Test

| ID | Method | Kết quả theo regression |
|---|---|---|
| MS-S-01 | `createSlotCalculatesEmptyStatusAndStoresInitialVolume` | PASS |
| MS-S-02 | `createSlotRecordsNonZeroInitialVolumeAsInventoryAdjustment` | PASS |
| MS-S-03 | `createSlotRejectsProductFromAnotherTenant` | PASS |
| MS-S-04 | `createSlotRejectsDuplicateSlotNumber` | PASS |
| MS-S-05 | `createSlotRejectsThresholdAboveCapacity` | PASS |
| MS-S-06 | `updateSlotDoesNotAllowCapacityBelowCurrentVolume` | PASS |
| MS-S-07 | `updateSlotPreservesCurrentVolumeAndRecalculatesStatus` | PASS |
| MS-S-08 | `disableSlotRefusesToDiscardRemainingInventory` | PASS |
| MS-S-09 | `disableSlotSoftDisablesAnEmptySlot` | PASS |
| MS-S-10 | `updateSlotStatusRecalculatesInventoryStatuses` | PASS |
| MS-S-11 | `listSlotsRejectsCrossTenantProductReference` | PASS |
| MS-S-12 | `tenantCannotReadAnotherTenantsSlot` | PASS |
| MS-S-13 | `createSlotRejectsPositiveInventoryWithoutProduct` | PASS |
| MS-S-14 | `createSlotRejectsInactiveProduct` | PASS |
| MS-S-15 | `updateSlotRejectsProductChangeWhileInventoryRemains` | PASS |
| MS-S-16 | `updateSlotRejectsProductRemovalWhileInventoryRemains` | PASS |
| MS-S-17 | `updateSlotAllowsProductChangeWhenEmpty` | PASS |
| MS-S-18 | `createSlotRejectsDisabledMachine` | PASS |
| MS-S-19 | `updateSlotRejectsDisabledMachine` | PASS |
| MS-S-20 | `updateSlotStatusRejectsDisabledMachine` | PASS |
| MS-S-21 | `createSlotRejectsNegativeThreshold` | PASS |
| MS-S-22 | `createSlotRejectsZeroCapacity` | PASS |
| MS-S-23 | `createSlotRejectsNegativeCurrentVolume` | PASS |
| MS-S-24 | `createSlotRejectsCurrentVolumeAboveCapacity` | PASS |
| MS-S-25 | `updateSlotPreservesErrorStatus` | PASS |
| MS-S-26 | `updateSlotRejectsDisabledSlot` | PASS |
| MS-S-27 | `updateSlotStatusRejectsDisabledSlot` | PASS |
| MS-S-28 | `disableSlotIsIdempotent` | PASS |
| MS-S-29 | `updateSlotStatusRejectsInvalidStatus` | PASS |
| MS-S-30 | `tenantCannotCreateSlotOnAnotherTenantsMachine` | PASS |
| MS-S-31 | `tenantCannotListSlotsOfAnotherTenantsMachine` | PASS |
| MS-S-32 | `tenantCannotUpdateAnotherTenantsSlot` | PASS |
| MS-S-33 | `tenantCannotUpdateAnotherTenantsSlotStatus` | PASS |
| MS-S-34 | `tenantCannotDisableAnotherTenantsSlot` | PASS |
| MS-S-35 | `updateSlotRejectsProductFromAnotherTenant` | PASS |
| MS-S-36 | `superAdminCanReadSlotFromAnotherTenant` | PASS |
| MS-S-37 | `superAdminCannotAssignProductFromDifferentTenant` | PASS |
| MS-S-38 | `technicianCannotCreateSlot` | PASS |
| MS-S-39 | `technicianCannotUpdateSlotConfiguration` | PASS |
| MS-S-40 | `technicianCannotDisableSlot` | PASS |
| MS-S-41 | `inventoryStaffCannotDisableSlot` | PASS |
| MS-S-42 | `inventoryStaffCannotUpdateSlotStatus` | PASS |

### Controller và Security

| Nhóm | Lượt | Kết quả |
|---|---:|---|
| Controller response/validation | 4 | PASS |
| RBAC theo 6 endpoint | 30 | PASS |
| Unauthenticated | 6 | PASS |
| ResourceNotFound mapping | 6 | PASS |
| Conflict mapping | 3 | PASS |
| Bad request | 2 | PASS |
| Access denied mapping | 1 | PASS |

| Thao tác | Super Admin | Brand Admin | Operations | Technician | Inventory |
|---|---|---|---|---|---|
| List/detail | Global | Own | Own | Own | Own |
| Create/update | Global | Own | Own | 403 | Own |
| PATCH status | Global | Own | Own | Own | 403 |
| DELETE | Global | Own | Own | 403 | 403 |

Giới hạn: Mockito mock repository và service; MockMvc security không chứng minh JWT thật hoặc truy vấn DB thật. Các test mapping 404/409 dùng exception giả lập; kiểm tra tenant nghiệp vụ nằm ở service tests. Chưa có ảnh Postman TASK-020 hoặc bằng chứng rollback/concurrency MySQL trong ZIP; không đánh dấu các kiểm thử đó PASS. Không có số liệu JaCoCo.
