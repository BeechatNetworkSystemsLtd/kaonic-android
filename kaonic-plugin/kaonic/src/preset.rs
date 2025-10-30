use reticulum::iface::kaonic::kaonic_grpc::proto::{
    configuration_request::{self, PhyConfig},
    RadioPhyConfigFsk, RadioPhyConfigOfdm, RadioPhyConfigQpsk,
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

pub const RADIO_PRESETS: [RadioPreset; 9] = [
    //
    RadioPreset {
        name: "OFDM Opt1 MCS0",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 22,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 0, opt: 0 }),
    },
    RadioPreset {
        name: "OFDM Opt1 MCS1",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 22,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 1, opt: 0 }),
    },
    RadioPreset {
        name: "OFDM Opt1 MCS2",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 22,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 2, opt: 0 }),
    },
    RadioPreset {
        name: "OFDM Opt1 MCS3",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 22,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 3, opt: 0 }),
    },
    RadioPreset {
        name: "OFDM Opt1 MCS4",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 20,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 4, opt: 0 }),
    },
    RadioPreset {
        name: "OFDM Opt1 MCS5",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 19,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 5, opt: 0 }),
    },
    RadioPreset {
        name: "OFDM Opt1 MCS6",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 17,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 6, opt: 0 }),
    },
    //
    RadioPreset {
        name: "OFDM Opt2 MCS0",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 21,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 0, opt: 1 }),
    },
    RadioPreset {
        name: "OFDM Opt2 MCS1",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 21,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 1, opt: 1 }),
    },
    RadioPreset {
        name: "OFDM Opt2 MCS2",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 21,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 2, opt: 1 }),
    },
    RadioPreset {
        name: "OFDM Opt2 MCS3",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 21,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 3, opt: 1 }),
    },
    RadioPreset {
        name: "OFDM Opt2 MCS4",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 19,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 4, opt: 1 }),
    },
    RadioPreset {
        name: "OFDM Opt2 MCS5",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 18,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 5, opt: 1 }),
    },
    RadioPreset {
        name: "OFDM Opt2 MCS6",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 16,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 6, opt: 1 }),
    },
    //
    RadioPreset {
        name: "OFDM Opt3 MCS0",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 21,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 0, opt: 2 }),
    },
    RadioPreset {
        name: "OFDM Opt3 MCS1",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 21,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 1, opt: 2 }),
    },
    RadioPreset {
        name: "OFDM Opt3 MCS2",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 21,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 2, opt:2}),
    },
    RadioPreset {
        name: "OFDM Opt3 MCS3",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 21,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 3, opt: 2 }),
    },
    RadioPreset {
        name: "OFDM Opt3 MCS4",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 21,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 4, opt: 2 }),
    },
    RadioPreset {
        name: "OFDM Opt3 MCS5",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 19,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 5, opt: 2 }),
    },
    RadioPreset {
        name: "OFDM Opt3 MCS6",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 18,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 6, opt: 2 }),
    },
    //
    RadioPreset {
        name: "OFDM Opt4 MCS0",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 20,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 0, opt: 3 }),
    },
    RadioPreset {
        name: "OFDM Opt4 MCS1",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 20,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 1, opt: 3 }),
    },
    RadioPreset {
        name: "OFDM Opt4 MCS2",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 20,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 2, opt: 3 }),
    },
    RadioPreset {
        name: "OFDM Opt4 MCS3",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 20,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 3, opt:3 }),
    },
    RadioPreset {
        name: "OFDM Opt4 MCS4",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 18,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 4, opt: 3 }),
    },
    RadioPreset {
        name: "OFDM Opt4 MCS5",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 17,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 5, opt: 3 }),
    },
    RadioPreset {
        name: "OFDM Opt4 MCS6",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 15,
        phy_config: PhyConfig::Ofdm(RadioPhyConfigOfdm { mcs: 6, opt: 3 }),
    },
    //
    RadioPreset {
        name: "QPSK KChip100 RateMode0",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 100,
            rate_mode: 0,
        }),
    },
    RadioPreset {
        name: "QPSK KChip100 RateMode1",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 100,
            rate_mode: 1,
        }),
    },
    RadioPreset {
        name: "QPSK KChip100 RateMode2",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 100,
            rate_mode: 2,
        }),
    },
    RadioPreset {
        name: "QPSK KChip100 RateMode3",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 100,
            rate_mode: 3,
        }),
    },
    //
    RadioPreset {
        name: "QPSK KChip200 RateMode0",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 200,
            rate_mode: 0,
        }),
    },
    RadioPreset {
        name: "QPSK KChip200 RateMode1",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 200,
            rate_mode: 1,
        }),
    },
    RadioPreset {
        name: "QPSK KChip200 RateMode2",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 200,
            rate_mode: 2,
        }),
    },
    RadioPreset {
        name: "QPSK KChip200 RateMode3",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 200,
            rate_mode: 3,
        }),
    },
    //
    RadioPreset {
        name: "QPSK KChip1000 RateMode0",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 1000,
            rate_mode: 0,
        }),
    },
    RadioPreset {
        name: "QPSK KChip1000 RateMode1",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 1000,
            rate_mode: 1,
        }),
    },
    RadioPreset {
        name: "QPSK KChip1000 RateMode2",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 1000,
            rate_mode: 2,
        }),
    },
    RadioPreset {
        name: "QPSK KChip1000 RateMode3",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 1000,
            rate_mode: 3,
        }),
    },
    //
    RadioPreset {
        name: "QPSK KChip2000 RateMode0",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 2000,
            rate_mode: 0,
        }),
    },
    RadioPreset {
        name: "QPSK KChip2000 RateMode1",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 2000,
            rate_mode: 1,
        }),
    },
    RadioPreset {
        name: "QPSK KChip2000 RateMode2",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 2000,
            rate_mode: 2,
        }),
    },
    RadioPreset {
        name: "QPSK KChip2000 RateMode3",
        freq: 869535,
        channel_spacing: 200,
        tx_power: 14,
        phy_config: PhyConfig::Qpsk(RadioPhyConfigQpsk {
            chip_freq: 2000,
            rate_mode: 3,
        }),
    },
];
