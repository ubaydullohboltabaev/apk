package uz.kunlikreja.app;

/** O'rta vidjet (2x2): sarlavha va 4 ta qator. */
public class TaskWidgetMedium extends TaskWidgetProvider {
    @Override
    protected int layoutId() { return R.layout.widget_medium; }

    @Override
    protected int rowCount() { return 4; }
}
