from rest_framework import serializers



from django.conf import settings

from .models import Profile, Event,Picture
'''
def upload(instance,filename):
    ext = filename.split('.')[-1]

    while True:
        name = '{}.{}'.format(str(instance.pk)+str(random(10)),ext)
        fileName = os.path.join(STORAGE,name)
        try:
            pic = Picture.objects.get(photo=fileName)
        except Picture.DoesNotExist:
            return fileName
            '''

class ProfileSerializer(serializers.ModelSerializer):



    class Meta:
        model = Profile
        fields = ('username','email','first_name')


'''
class PictureSerializer(serializers.Serializer):
    photo = serializers.CharField()

    def valid(self):
        if self.validated_data['photo'] not in ['php','exe','sh','cgi']:
                image = Image.open(BytesIO(base64.b64decode(self.validated_data['photo'])))

        		#string as file
        		image_io = BytesIO()
        		image_io.seek(0)

        		#save image to stringIO file as JPEG
        		image.save(image_io,format='JPEG')
        		#create picture
        		picture = Picture.objects.create()

        		#convert image to django recognized format
        		django_image = InMemoryUploadedFile(image_io,None,IMAGE_STORAGE+"/Picture"+str(picture.pk).zfill(4)+'.jpeg','image/jpeg',image_io.getbuffer().nbytes,None)




            return True
        return False
        '''

class EventSerializer(serializers.ModelSerializer):
    #creator_s = ProfileSerializer(source='creator')
    class Meta:
        model = Event
        fields = ('creator','event_name')
    def create(self):
        return Event(**self.validated_data)

class UserEventSerializer(serializers.ModelSerializer):
    class Meta:
        model = Event
        fields = ('event_name',)


class PasswordSerializer(serializers.Serializer):
    new_password = serializers.CharField()
    old_password = serializers.CharField(max_length=None)


    def valid(self,user):

        if not user.check_password(self.validated_data['old_password']):
            return False
        return True
