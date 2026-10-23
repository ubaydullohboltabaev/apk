package uz.kunlikreja.app;

/** Kichik vidjet (2x1): faqat 3 ta qator. */
public class TaskWidgetSmall extends TaskWidgetProvider {
    @Override
    protected int layoutId() { return R.layout.widget_small; }

    @Override
    protected int rowCount() { return 3; }
}
