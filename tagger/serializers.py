from rest_framework import serializers



from django.conf import settings

from .models import Profile, Event



class ProfileSerializer(serializers.ModelSerializer):



    class Meta:
        model = Profile
        fields = ('username','email','first_name')

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
