package device.ojtproject.service;

import device.ojtproject.entity.Device;
import device.ojtproject.exception.DeviceException;
import device.ojtproject.repository.DeviceRepository;
import device.ojtproject.service.dto.DeviceDto;
import device.ojtproject.service.dto.DeviceFactory;
import device.ojtproject.service.dto.DeviceSearchDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static device.ojtproject.exception.DeviceErrorCode.DUPLICATED_SN;
import static device.ojtproject.exception.DeviceErrorCode.NO_MEMBER;


@Service
@AllArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    //----------------------------------------์กฐํ

    @Override
    public List<DeviceSearchDto> searchDevice(String serialNumber, String qrCode, String macAddress) {
        List<Device> devices;
        if (serialNumber != null) devices = deviceRepository.findBySerialNumberContaining(serialNumber);
        else if (qrCode != null) devices = deviceRepository.findByQrCodeContaining(qrCode);
        else if (macAddress != null) devices = deviceRepository.findByMacAddressContaining(macAddress);
        else devices = deviceRepository.findAll();

        return devices
                .stream().map(DeviceFactory::getDeviceSearchDto)
                .collect(Collectors.toList());
    }


    //----------------------------์์ฑ
    @Transactional
    @Override
    public DeviceDto createDevice(DeviceDto deviceDto) {
        validateCreateDeviceRequest(deviceDto);
        Device device = DeviceFactory.getDevice(deviceDto);
        deviceRepository.save(device);
        return DeviceFactory.getDeviceDto(device);
    }

    private void validateCreateDeviceRequest(DeviceDto deviceDto) {
        //business validation
        /*validDeviceLevel(
                deviceDto.getSerialNumber(),
                deviceDto.getQrCode(),
                deviceDto.getMacAddress()
        );*/

        deviceRepository.findBySerialNumber(deviceDto.getSerialNumber())
                .ifPresent((device -> {
                    throw new DeviceException(DUPLICATED_SN);
                }));
    }

    //------------------------------์์?
    @Transactional
    @Override
    public DeviceDto editDevice(DeviceDto deviceDto, String serialNumber) {
        validateDeviceEditRequest(deviceDto);
        Device device = deviceRepository.findBySerialNumber(serialNumber).orElseThrow(
                () -> new DeviceException(NO_MEMBER)
        );
        device.edit(DeviceFactory.getDevice(deviceDto));
        deviceRepository.save(device);
        //Device savedDevice = deviceRepository.save(device);

        return DeviceFactory.getDeviceDto(device);
    }

    private void validateDeviceEditRequest(DeviceDto deviceDto) {
        /*validDeviceLevel(
                deviceDto.getSerialNumber(),
                deviceDto.getQrCode(),
                deviceDto.getMacAddress()
        );*/


    }

    //--------------------------------------์ญ์?
    @Transactional
    @Override
    public DeviceDto discardDevice(String serialNumber) {
        //NORMAL -> DELETE
        //DELETE ํ์ด๋ธ์ ์ํ๊ฐ ์?์ฅ ๋จ.
        Device device = deviceRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new DeviceException(NO_MEMBER));
        device.changeToDiscard();
        deviceRepository.save(device);

        return DeviceFactory.getDeviceDto(device);
    }

    //------------------------๋์ ์?์ง
    @Transactional
    @Override
    public DeviceDto inactiveDevice(String serialNumber) {
        //active -> inactive
        Device device = deviceRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new DeviceException(NO_MEMBER));
        device.changeToINActive();
        deviceRepository.save(device);

        return DeviceFactory.getDeviceDto(device);

    }
}
    /// ์ฌ์ฉ ์ํจ...
    /*private void validDeviceLevel(String serialNumber, String qrCode, String macAddress) {
        if(serialNumber == null){
            throw new DeviceException(NO_SERIALNUMBER);}
        if(qrCode == null){
            throw new DeviceException(NO_QRCODE);}
        if(macAddress == null){
            throw new DeviceException(NO_MACADDRESS);}
//        if(activeStatus == null){
//            throw new DeviceException(ACTIVE_NULL_ERROR);}
    }
    ///?????*/


