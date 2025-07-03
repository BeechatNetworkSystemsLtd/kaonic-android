use reticulum::iface::kaonic::kaonic_grpc::proto::{
    configuration_request::{self, PhyConfig},
    RadioPhyConfigFsk, RadioPhyConfigOfdm,
};
use serde::Serialize;

#[derive(Serialize)]
pub struct RadioPreset {
    name: &'static str,
    freq: u32,
    channel_spacing: u32,
    tx_power: u32,
    phy_config: configuration_request::PhyConfig,
}

pub const RADIO_PRESETS: [RadioPreset; 2] = [
    RadioPreset {
        name: "OFDM 1",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 10,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 6, opt: 0 }),
    },
    RadioPreset {
        name: "FSK FEC 400kHz Sample Rate 100kHz",
        freq: 863225,
        channel_spacing: 400,
        tx_power: 10,
        phy_config: PhyConfig::Fsk(RadioPhyConfigFsk {
            bt: 0x03,
            midxs: 0x01,
            midx: 0x03,
            mord: 0x00,
            preamble_length: 0,
            freq_inversion: false,
            srate: 0x01,
            pdtm: 0x00,
            rxo: 0x02,
            rxpto: 0x00,
            mse: 0x00,
            preamble_inversion: false,
            fecs: 0x01,
            fecie: true,
            sfdt: 0x08,
            pdt: 0x05,
            sftq: false,
            sfd32: 0x00,
            rawbit: false,
            csfd1: 0x02,
            csfd0: 0x02,
            sfd0: 0x7209,
            sfd1: 0xF672,
            sfd: 0x00,
            dw: 0x01,
            pe: false,
            en: false,
            fskpe0: 0x0E,
            fskpe1: 0x0F,
            fskpe2: 0xF0,
        }),
    },
];
