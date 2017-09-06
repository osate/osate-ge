package org.osate.ge.gef;

import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.common.adapt.inject.AdapterMaps;
import org.eclipse.gef.mvc.fx.MvcFxModule;
import org.eclipse.gef.mvc.fx.behaviors.HoverBehavior;
import org.eclipse.gef.mvc.fx.behaviors.SelectionBehavior;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.handlers.FocusAndSelectOnClickHandler;
import org.eclipse.gef.mvc.fx.handlers.HoverOnHoverHandler;
import org.eclipse.gef.mvc.fx.handlers.ResizeTranslateFirstAnchorageOnHandleDragHandler;
import org.eclipse.gef.mvc.fx.handlers.TranslateSelectedOnDragHandler;
import org.eclipse.gef.mvc.fx.parts.DefaultFocusFeedbackPartFactory;
import org.eclipse.gef.mvc.fx.parts.DefaultHoverFeedbackPartFactory;
import org.eclipse.gef.mvc.fx.parts.DefaultSelectionFeedbackPartFactory;
import org.eclipse.gef.mvc.fx.parts.DefaultSelectionHandlePartFactory;
import org.eclipse.gef.mvc.fx.parts.IContentPartFactory;
import org.eclipse.gef.mvc.fx.parts.SquareSegmentHandlePart;
import org.eclipse.gef.mvc.fx.policies.ResizePolicy;
import org.eclipse.gef.mvc.fx.policies.TransformPolicy;
import org.eclipse.gef.mvc.fx.providers.GeometricOutlineProvider;
import org.eclipse.gef.mvc.fx.providers.ShapeBoundsProvider;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.osate.ge.gef.parts.AgeShapeContentPart;

import com.google.inject.Provider;
import com.google.inject.multibindings.MapBinder;

public class AgeModule extends MvcFxModule {
	@Override
	protected void configure() {
		super.configure();

		bindSquareSegmentHandlePartAdapters(AdapterMaps.getAdapterMapBinder(binder(), SquareSegmentHandlePart.class));

		binder().bind(IContentPartFactory.class).to(AgeContentPartFactory.class);

		bindAgeShapePartAdapters(AdapterMaps.getAdapterMapBinder(binder(), AgeShapeContentPart.class,
				AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE)));
//		bindTestConnectionPart(AdapterMaps.getAdapterMapBinder(binder(), TestConnectionPart.class,
//				AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE)));
	}

//	@Override
//	protected void bindContentBehaviorAsIRootPartAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
//		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TestContentBehavior.class);
//	}

	@Override
	protected void bindAbstractContentPartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		super.bindAbstractContentPartAdapters(adapterMapBinder);

		// select on click
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(FocusAndSelectOnClickHandler.class);
		// select on type
		// adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(SelectFocusedOnTypeHandler.class);
	}

	protected void bindSquareSegmentHandlePartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		// single selection: resize relocate on handle drag without modifier
		adapterMapBinder.addBinding(AdapterKey.defaultRole())
		.to(ResizeTranslateFirstAnchorageOnHandleDragHandler.class);

		// TODO: Don't want rotate
		// rotate on drag + control
		// adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(RotateSelectedOnHandleDragHandler.class);

		// multi selection: scale relocate on handle drag without modifier
		// adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ResizeTransformSelectedOnHandleDragHandler.class);
	}

	protected void bindAgeShapePartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {

		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TranslateSelectedOnDragHandler.class);

		adapterMapBinder
		.addBinding(AdapterKey.role(DefaultSelectionFeedbackPartFactory.SELECTION_FEEDBACK_GEOMETRY_PROVIDER))
		.to(FocusAndSelectOnClickHandler.class);

		final Provider<ShapeBoundsProvider> shapeBoundsProviderProvider = () -> new ShapeBoundsProvider(-0.5);

		adapterMapBinder
		.addBinding(AdapterKey.role(DefaultSelectionFeedbackPartFactory.SELECTION_FEEDBACK_GEOMETRY_PROVIDER))
		.toProvider(shapeBoundsProviderProvider);
		adapterMapBinder
		.addBinding(AdapterKey.role(DefaultSelectionHandlePartFactory.SELECTION_HANDLES_GEOMETRY_PROVIDER))
		.toProvider(shapeBoundsProviderProvider);
		adapterMapBinder.addBinding(AdapterKey.role(DefaultFocusFeedbackPartFactory.FOCUS_FEEDBACK_GEOMETRY_PROVIDER))
		.toProvider(shapeBoundsProviderProvider);

		adapterMapBinder
		.addBinding(
				AdapterKey.role(DefaultSelectionFeedbackPartFactory.SELECTION_LINK_FEEDBACK_GEOMETRY_PROVIDER))
		.to(GeometricOutlineProvider.class);

		// register resize/transform policies (writing changes also to model)
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TransformPolicy.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ResizePolicy.class);

		// adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ConnectedSupport.class);

		// TODO
//		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TestAnchorProvider.class);

		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(HoverBehavior.class);

		// Hover related
		// TODO:
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(HoverOnHoverHandler.class);

		// adapterMapBinder.addBinding(AdapterKey.role(HoverBehavior.HOVER_FEEDBACK_PART_FACTORY))
		// .to(DefaultHoverFeedbackPartFactory.class);

		// TODO
		// adapterMapBinder.addBinding(AdapterKey.role(DefaultHoverFeedbackPartFactory.HOVER_FEEDBACK_GEOMETRY_PROVIDER))
		// .to(AgeShapeGeometricOutlineProvider.class);

	}

	protected void bindTestConnectionPart(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		// Hover related
		// TODO:
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(HoverOnHoverHandler.class);
		adapterMapBinder.addBinding(AdapterKey.role(DefaultHoverFeedbackPartFactory.HOVER_FEEDBACK_GEOMETRY_PROVIDER))
		.to(GeometricOutlineProvider.class);

	}

	@Override
	protected void bindSelectionHandlePartFactoryAsContentViewerAdapter(
			MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(SelectionBehavior.SELECTION_HANDLE_PART_FACTORY))
		.to(DefaultSelectionHandlePartFactory.class);
	}
}
