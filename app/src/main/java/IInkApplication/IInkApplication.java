package IInkApplication;

import android.app.Application;

import com.myscript.iink.Configuration;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;
import com.myscript.iink.Renderer;

import java.io.File;

import IInkApplication.certificate.MyCertificate;

public class IInkApplication extends Application {
    private Renderer renderer;
    private Editor editor;

    private static Engine engine;

    public IInkApplication(String packageCodePath, String filesDirPath, Renderer renderer, Editor editor) {
        this.renderer = renderer;
        this.editor = editor;
        engine = IInkApplication.getEngine();
        Configuration configuration = engine.getConfiguration();

        configuration.setStringArray("configuration-manager.search-path", new String[] {"zip://" + packageCodePath + "!/assets/conf"});

        String tempDir = filesDirPath + File.separator + "tmp";
        configuration.setString("content-package.temp-folder", tempDir);

        configuration.setNumber("math.solver.fractional-part-digits", 2);
    }

    public static synchronized Engine getEngine() {
        if (engine == null) {
            engine = Engine.create(MyCertificate.getBytes());
        }
        return engine;
    }

    public Editor getEditor() {
        return editor;
    }

    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }
}
