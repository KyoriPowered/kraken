package net.kyori.kraken.util;

import com.intellij.CommonBundle;
import com.intellij.reference.SoftReference;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.util.ResourceBundle;

public class KrakenMessages {
  private static final String BUNDLE_NAME = "messages.Kraken";
  private static Reference<ResourceBundle> _bundle;

  public static String get(final @PropertyKey(resourceBundle = BUNDLE_NAME) String key) {
    return CommonBundle.message(bundle(), key);
  }

  private static ResourceBundle bundle() {
    ResourceBundle bundle = SoftReference.dereference(_bundle);
    if(bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE_NAME);
      _bundle = new SoftReference<>(bundle);
    }
    return bundle;
  }
}
