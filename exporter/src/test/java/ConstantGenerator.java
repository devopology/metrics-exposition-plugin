import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ConstantGenerator {

    public static void main(String[] args) throws Exception {
        args = new String[1];
        args[0] = "test-application/configuration/exporter.yml";
        new ConstantGenerator().execute(args);
    }

    private void execute(String[] args) throws Exception {
        // Load the YAML configuration using a custom Resolver to return String values
        Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new LoaderOptions(), new org.yaml.snakeyaml.resolver.Resolver() {
            protected void addImplicitResolvers() {
                this.addImplicitResolver(Tag.MERGE, MERGE, "<");
                this.addImplicitResolver(Tag.YAML, YAML, "!&*");
            }
        });

        List<String> list = new ArrayList<>();
        Map<String, Object> yamlMap = yaml.load(new FileReader(args[0]));
        traverse("", yamlMap, list);

        Collections.sort(list);

        for (String string : list) {
            System.out.println(string);
        }
    }

    private void traverse(String path, Object object, List<String> list) {
        if (object instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) object;
            Set<String> mapKeySet = map.keySet();
            for (String key : mapKeySet) {
                traverse(path + "/" + key, map.get(key), list);
            }
        }

        if (object instanceof String) {
            String upperPath = path.toUpperCase().replace(
                    "/", "_")
                        .replaceFirst(Pattern.quote("_"), "")
                        .replaceFirst(Pattern.quote("-"), "_");

            list.add(String.format("public static final String %s_PATH = \"%s\";", upperPath, path));
        }
    }
}
