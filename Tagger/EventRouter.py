from rest_framework.routers import Route, SimpleRouter, DynamicDetailRoute

class EventRouter(SimpleRouter):

    routes = [
        Route(
            url=r'^{prefix}/create',
            mapping={'post':'create'},
            name='{basename}-create',
            initkwargs={}
        ),

        Route(
            url=r'^{prefix}/{lookup}/putimagefile$',
            mapping={'put':'put_image_file'},
            name='{basename}-put_image_file',
            initkwargs={}

        ),
        Route(
            url=r'^{prefix}/{lookup}/postimagedata$',
            mapping={'post':'post_image_data'},
            name='{basename}-post_image_data',
            initkwargs={}
        ),
        DynamicDetailRoute(
            url=r'^{prefix}/{methodnamehyphen}$',
            name='{basename}-{methodnamehyphen}',
            initkwargs={}
        )

    ]
