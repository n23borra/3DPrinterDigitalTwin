public class PrinterState {

    public String state;
    public Double nozzleTemp;
    public Double bedTemp;
    public Double posX;
    public Double posY;
    public Double posZ;

    @Override
    public String toString() {
        return "PrinterState{" +
                "state='" + state + '\'' +
                ", nozzleTemp=" + nozzleTemp +
                ", bedTemp=" + bedTemp +
                ", posX=" + posX +
                ", posY=" + posY +
                ", posZ=" + posZ +
                '}';
    }
}
