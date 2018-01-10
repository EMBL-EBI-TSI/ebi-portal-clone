package uk.ac.ebi.tsc.portal.clouddeployment.model;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public enum MachineSpecs {

    OS_EMBASSY_S1_NANO("s1.nano", 1, 1, 5),
    OS_EMBASSY_S1_TINY("s1.tiny", 1, 1, 10),
    OS_EMBASSY_S1_SMALL("s1.small", 2, 2, 20),
    OS_EMBASSY_S1_MODEST("s1.modest", 2, 4, 30),
    OS_EMBASSY_S1_MEDIUM("s1.medium", 4, 4, 40),
    OS_EMBASSY_S1_CAPACIOUS("s1.capacious", 6, 4, 50),
    OS_EMBASSY_S1_LARGE("s1.large", 8, 4, 60),
    OS_EMBASSY_S1_JUMBO("s1.jumbo", 12, 6, 70),
    OS_EMBASSY_S1_HUGE("s1.huge", 16, 8, 80),
    OS_EMBASSY_S1_MASSIVE("s1.massive", 32, 8, 100),
    OS_EMBASSY_S1_GARGANTUAN("s1.gargantuan", 64, 16, 100),

    GCP_N1_STANDARD_1("n1-standard-1", 3.75, 1, 64),
    GCP_N1_STANDARD_2("n1-standard-2", 7.50, 2, 64),
    GCP_N1_STANDARD_4("n1-standard-4", 15, 4, 64),
    GCP_N1_STANDARD_8("n1-standard-8", 30, 8, 64),
    GCP_N1_STANDARD_16("n1-standard-16", 60, 16, 64),
    GCP_N1_STANDARD_32("n1-standard-32", 120, 32, 64),
    GCP_N1_STANDARD_64("n1-standard-64", 240, 64, 64),

    GCP_N1_HIGHMEM_2("n1-highmem-2", 13, 2, 64),
    GCP_N1_HIGHMEM_4("n1-highmem-4", 26, 4, 64),
    GCP_N1_HIGHMEM_8("n1-highmem-8", 52, 8, 64),
    GCP_N1_HIGHMEM_16("n1-highmem-16", 104, 16, 64),
    GCP_N1_HIGHMEM_32("n1-highmem-32", 208, 32, 64),
    GCP_N1_HIGHMEM_64("n1-highmem-64", 416, 64, 64),

    GCP_N1_HIGHCPU_2("n1-highcpu-2", 1.8, 2, 64),
    GCP_N1_HIGHCPU_4("n1-highcpu-4", 3.6, 4, 64),
    GCP_N1_HIGHCPU_8("n1-highcpu-8", 7.2, 8, 64),
    GCP_N1_HIGHCPU_16("n1-highcpu-16", 14.4, 16, 64),
    GCP_N1_HIGHCPU_32("n1-highcpu-32", 28.8, 32, 64),
    GCP_N1_HIGHCPU_64("n1-highcpu-64", 57.6, 64, 64),

    GCP_F1_MICRO("f1-micro", 0.60, 0.2, 64),
    GCP_G1_SMALL("g1-small", 0.5, 0.5, 64),

    AWS_T2_NANO("t2.nano", 0.5, 1, 0),
    AWS_T2_MICRO("t2.micro", 1, 1, 0),
    AWS_T2_SMALL("t2.small", 2, 1, 0),
    AWS_T2_MEDIUM("t2.medium", 4, 2, 0),
    AWS_T2_LARGE("t2.large", 8, 2, 0),
    AWS_T2_XLARGE("t2.xlarge", 16, 4, 0),
    AWS_T2_2XLARGE("t2.2xlarge", 32, 8, 0),

    AWS_M4_LARGE("m4.large", 8, 2, 0),
    AWS_M4_XLARGE("m4.xlarge", 16, 4, 0),
    AWS_M4_2XLARGE("m4.2xlarge", 32, 8, 0),
    AWS_M4_4XLARGE("m4.4xlarge", 64, 16, 0),
    AWS_M4_8XLARGE("m4.8xlarge", 128, 40, 0),
    AWS_M4_16XLARGE("m4.16xlarge", 256, 64, 0),

    AWS_M3_MEDIUM("m3.medium", 3.75, 1, 4),
    AWS_M3_LARGE("m3.large", 7.5, 2, 32),
    AWS_M3_XLARGE("m3.xlarge", 15, 4, 80),
    AWS_M3_2XLARGE("m3.2xlarge", 30, 8, 160),

    AWS_C4_LARGE("c4.large", 3.75, 2, 0),
    AWS_C4_XLARGE("c4.xlarge", 7.5, 4, 0),
    AWS_C4_2XLARGE("c4.2xlarge", 15, 8, 0),
    AWS_C4_4XLARGE("c4.4xlarge", 30, 16, 0),
    AWS_C4_8XLARGE("c4.8xlarge", 60, 32, 0),

    AWS_C3_LARGE("c3.large", 3.75, 2, 32),
    AWS_C3_XLARGE("c3.xlarge", 7.5, 4, 80),
    AWS_C3_2XLARGE("c3.2xlarge", 15, 8, 160),
    AWS_C3_4XLARGE("c3.4xlarge", 30, 16, 320),
    AWS_C3_8XLARGE("c3.8xlarge", 60, 32, 640),

    AWS_X1_32XLARGE("c3.4xlarge", 1952, 128, 3840),
    AWS_X1_16XLARGE("c3.8xlarge", 976, 64, 1920),

    AWS_R4_LARGE("r4.large", 15.25, 2, 0),
    AWS_R4_XLARGE("r4.xlarge", 30.5, 4, 0),
    AWS_R4_2XLARGE("r4.2xlarge", 61, 8, 0),
    AWS_R4_4XLARGE("r4.4xlarge", 122, 16, 0),
    AWS_R4_8XLARGE("r4.8xlarge", 244, 40, 0),
    AWS_R4_16XLARGE("r4.16xlarge", 488, 64, 0),

    AWS_R3_LARGE("r3.large", 15.25, 2, 32),
    AWS_R3_XLARGE("r3.xlarge", 30.5, 4, 80),
    AWS_R3_2XLARGE("r3.2xlarge", 61, 8, 160),
    AWS_R3_4XLARGE("r3.4xlarge", 122, 16, 320),
    AWS_R3_8XLARGE("r3.8xlarge", 244, 32, 640),

    AWS_P2_XLARGE("p2.xlarge", 61, 4, 0),
    AWS_P2_8XLARGE("p2.8xlarge", 488, 32, 0),
    AWS_P2_16XLARGE("p2.16xlarge", 732, 64, 0),

    AWS_G2_2XLARGE("g2.2xlarge", 15, 8, 60),
    AWS_G2_8XLARGE("g2.8xlarge", 60, 32, 240),

    AWS_F1_2XLARGE("f1.2xlarge", 122, 8, 470),
    AWS_F1_16XLARGE("f1.16xlarge", 976, 64, 3760),

    AWS_I3_LARGE("i3.large", 15.25, 2, 0.475),
    AWS_I3_XLARGE("i3.xlarge", 30.5, 4, 0.95),
    AWS_I3_2XLARGE("i3.2xlarge", 61, 8, 1.9),
    AWS_I3_4XLARGE("i3.4xlarge", 122, 16, 3.8),
    AWS_I3_8XLARGE("i3.8xlarge", 244, 40, 5.6),
    AWS_I3_16XLARGE("i3.16xlarge", 488, 64, 11.2),

    AWS_D2_XLARGE("d2.xlarge", 30.5, 4, 6000),
    AWS_D2_2XLARGE("d2.2xlarge", 61, 8, 12000),
    AWS_D2_4XLARGE("d2.4xlarge", 122, 16, 24000),
    AWS_D2_8XLARGE("d2.8xlarge", 244, 32, 48000),

    Standard_DS4_V2_Promo("Standard_DS4_V2_Promo", 28, 8, 56),
    Standard_DS12_V2_Promo("Standard_DS12_V2_Promo", 28, 4, 56)
    ;

    private final String flavourName;

    private final double ramGb;

    private final double vCPUSs;

    private final double diskSpaceGb;

    MachineSpecs(String flavourName, double ramGb, double vCPUSs, double diskSpaceGb) {
        this.flavourName = flavourName;
        this.ramGb = ramGb;
        this.vCPUSs = vCPUSs;
        this.diskSpaceGb = diskSpaceGb;
    }

    public String getFlavourName() {
        return flavourName;
    }

    public double getRamGb() {
        return ramGb;
    }

    public double getvCPUSs() {
        return vCPUSs;
    }

    public double getDiskSpaceGb() {
        return diskSpaceGb;
    }

    public static MachineSpecs fromFlavourName(String flavourName) {
        for (MachineSpecs machineSpecs : MachineSpecs.values()) {
            if (flavourName.equals(machineSpecs.flavourName)) {
                return machineSpecs;
            }
        }
        return null;
    }

}
