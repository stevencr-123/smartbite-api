package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.qr.CodigoQRResponseDTO;
import com.smartbite.operativo.dto.qr.GenerarQRRequestDTO;

public interface CodigoQRService {

    // ordenId debe ser enviado externamente (ej: path param en controller futuro)
    CodigoQRResponseDTO generarQR(GenerarQRRequestDTO request, Long ordenId);

    CodigoQRResponseDTO obtenerPorId(Long codigoQrId);
}

