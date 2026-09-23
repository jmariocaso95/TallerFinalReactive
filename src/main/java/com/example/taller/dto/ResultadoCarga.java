package com.example.taller.dto;

public record ResultadoCarga ( int guardados, int fallidos){
    public ResultadoCarga mas(ResultadoCarga otro){
        return new ResultadoCarga(this.guardados + otro.guardados, this.fallidos + otro.fallidos);
    }

}
