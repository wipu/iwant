package net.sf.iwant.api.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;

public abstract class TargetBase extends Target {

	public TargetBase(String name) {
		super(name);
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public final List<Path> ingredients() {
		IngredientsBuilder ib = new IngredientsBuilder();
		ingredientsAndParameters(ib);
		return ib.ingredients();
	}

	private class IngredientsBuilder extends BaseBuilder {

		private final List<Path> ingredients = new ArrayList<>();

		public List<Path> ingredients() {
			return ingredients;
		}

		@Override
		public IngredientsAndParametersPlease parameter(String name,
				Collection<? extends Object> parameters) {
			return this;
		}

		@Override
		public IngredientsAndParametersPlease ingredients(String name,
				Collection<? extends Path> ingredients) {
			if (ingredients != null) {
				this.ingredients.addAll(ingredients);
			}
			return this;
		}

		@Override
		public IngredientsAndParametersPlease optionalIngredients(String name,
				Collection<? extends Path> maybeIngredients) {
			if (maybeIngredients != null) {
				for (Path maybeIngredient : maybeIngredients) {
					if (maybeIngredient != null) {
						this.ingredients.add(maybeIngredient);
					}
				}
			}
			return this;
		}

	}

	@Override
	public final String contentDescriptor() {
		ContentDescriptorBuilder cdb = new ContentDescriptorBuilder();
		ingredientsAndParameters(cdb);
		return cdb.contentDescriptor();
	}

	private class ContentDescriptorBuilder extends BaseBuilder {

		private final StringBuilder b = new StringBuilder();

		ContentDescriptorBuilder() {
			b.append(TargetBase.this.getClass().getCanonicalName())
					.append("\n");
		}

		public String contentDescriptor() {
			return b.toString();
		}

		@Override
		public IngredientsAndParametersPlease parameter(String name,
				Collection<? extends Object> parameters) {
			return nameAndValues("p", name, parameters);
		}

		private String escaped(Object o) {
			return o.toString().replace("\\", "\\\\").replace("\n", "\\n");
		}

		private IngredientsAndParametersPlease nameAndValues(String type,
				String name, Collection<? extends Object> values) {
			b.append(type).append(":").append(escaped(name)).append(":\n");
			if (values == null) {
				b.append(" null-collection\n");
			} else {
				for (Object value : values) {
					if (value == null) {
						b.append(" null\n");
					} else {
						b.append("  ").append(escaped(value)).append("\n");
					}
				}
			}
			return this;
		}

		@Override
		public IngredientsAndParametersPlease ingredients(String name,
				Collection<? extends Path> ingredients) {
			return nameAndValues("i", name, ingredients);
		}

		@Override
		public IngredientsAndParametersPlease optionalIngredients(String name,
				Collection<? extends Path> maybeIngredients) {
			return ingredients(name, maybeIngredients);
		}

	}

	protected abstract IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse);

	protected interface IngredientsAndParametersPlease {

		IngredientsAndParametersDefined nothingElse();

		IngredientsAndParametersPlease parameter(String name,
				Collection<? extends Object> parameters);

		IngredientsAndParametersPlease parameter(String name,
				Object... parameters);

		IngredientsAndParametersPlease ingredients(String name,
				Collection<? extends Path> ingredients);

		IngredientsAndParametersPlease ingredients(String name,
				Path... ingredients);

		IngredientsAndParametersPlease optionalIngredients(String name,
				Path... maybeIngredients);

		IngredientsAndParametersPlease optionalIngredients(String name,
				Collection<? extends Path> maybeIngredients);

	}

	private abstract class BaseBuilder implements
			IngredientsAndParametersPlease {

		@Override
		public final IngredientsAndParametersDefined nothingElse() {
			return null;
		}

		@Override
		public final IngredientsAndParametersPlease parameter(String name,
				Object... parameters) {
			return parameter(name, Arrays.asList(parameters));
		}

		@Override
		public final IngredientsAndParametersPlease ingredients(String name,
				Path... ingredients) {
			return ingredients(name, Arrays.asList(ingredients));
		}

		@Override
		public IngredientsAndParametersPlease optionalIngredients(String name,
				Path... maybeIngredients) {
			return optionalIngredients(name, Arrays.asList(maybeIngredients));
		}

	}

	protected interface IngredientsAndParametersDefined {
		// just a marker of end
	}

}
