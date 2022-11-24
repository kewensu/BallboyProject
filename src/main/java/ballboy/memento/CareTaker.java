package ballboy.memento;

public class CareTaker {

    private Memento memento = null;

    public void save(Memento memento){
        this.memento = memento;
    }

    public Memento get(){
        return memento;
    }
}
